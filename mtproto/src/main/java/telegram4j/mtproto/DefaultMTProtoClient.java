package telegram4j.mtproto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.tcp.TcpClient;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.concurrent.Queues;
import telegram4j.mtproto.auth.AuthorizationKeyHolder;
import telegram4j.mtproto.util.AES256IGECipher;
import telegram4j.tl.MTProtoObject;
import telegram4j.tl.TlDeserializer;
import telegram4j.tl.TlObject;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static telegram4j.mtproto.util.CryptoUtil.*;
import static telegram4j.tl.TlSerialUtil.readInt128;

public class DefaultMTProtoClient implements MTProtoClient {

    private static final Logger log = Loggers.getLogger(DefaultMTProtoClient.class);

    private final TcpClient tcpClient;
    private final SessionResources mtProtoResources;
    private final ConcurrentMap<DataCenter, MTProtoSession> sessions = new ConcurrentHashMap<>();

    public DefaultMTProtoClient(MTProtoOptions options) {
        this.tcpClient = options.getTcpClient();
        this.mtProtoResources = options.getMtProtoResources();
    }

    @Override
    public Mono<MTProtoSession> getSession(DataCenter dc) {
        return Mono.fromSupplier(() -> sessions.get(dc))
                .switchIfEmpty(createSession(dc));
    }

    @Override
    public Mono<Void> close() {
        return Flux.fromIterable(sessions.values())
                .doOnNext(session -> session.getConnection().dispose())
                .then();
    }

    private Mono<MTProtoSession> createSession(DataCenter dc) {
        return tcpClient.remoteAddress(() -> new InetSocketAddress(dc.getAddress(), dc.getPort()))
                .doOnConnected(con -> {
                    log.debug("Connected to datacenter №{} ({}:{})", dc.getId(), dc.getAddress(), dc.getPort());
                    log.debug("Sending header identifier to the server.");

                    ByteBuf identifier = mtProtoResources.getTransport()
                            .identifier(con.channel().alloc());

                    con.channel().writeAndFlush(identifier);
                })
                .doOnDisconnected(con -> log.debug("Disconnected from the datacenter №{} ({}:{})",
                        dc.getId(), dc.getAddress(), dc.getPort()))
                .connect()
                .flatMap(con -> Mono.create(sink -> {
                    Sinks.Many<MTProtoObject> authReceiver = Sinks.many().multicast()
                            .onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

                    Sinks.Many<TlObject> rpcReceiver = Sinks.many().multicast()
                            .onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

                    MTProtoSession session = new MTProtoSession(con, authReceiver,
                            rpcReceiver, dc, mtProtoResources);

                    sessions.put(dc, session);

                    sink.onCancel(con.inbound().receive().retain()
                            .bufferUntil(mtProtoResources.getTransport()::canDecode)
                            .map(bufs -> con.channel().alloc().compositeBuffer(bufs.size())
                                    .addComponents(true, bufs))
                            .map(mtProtoResources.getTransport()::decode)
                            .flatMap(buf -> {
                                if (buf.readableBytes() == Integer.BYTES) { // The error code writes as negative int32
                                    int code = buf.readIntLE() * -1;
                                    return Mono.error(() -> TransportException.create(code));
                                }
                                return Mono.just(buf);
                            })
                            .doOnNext(buf -> {
                                long authKeyId = buf.readLongLE();

                                if (authKeyId == 0) { // unencrypted message
                                    buf.skipBytes(12); // message id (8) + payload length (4)

                                    MTProtoObject obj = TlDeserializer.deserialize(buf);
                                    authReceiver.emitNext(obj, Sinks.EmitFailureHandler.FAIL_FAST);
                                    return;
                                }

                                AuthorizationKeyHolder authorizationKey = session.getAuthorizationKey();
                                long longAuthKeyId = readLongLE(authorizationKey.getAuthKeyId());
                                if (authKeyId != longAuthKeyId) {
                                    throw new IllegalStateException("Incorrect auth key id. Received: "
                                            + authKeyId + ", but excepted: " + longAuthKeyId);
                                }

                                byte[] messageKey = readInt128(buf);

                                ByteBufAllocator alloc = buf.alloc();
                                ByteBuf authKeyBuf = alloc.buffer()
                                        .writeBytes(authorizationKey.getAuthKey());

                                AES256IGECipher cipher = createAesCipher(messageKey, authKeyBuf, true);

                                byte[] decrypted = cipher.decrypt(toByteArray(buf));
                                byte[] messageKeyCLarge = sha256Digest(concat(toByteArray(authKeyBuf.slice(96, 32)), decrypted));
                                byte[] messageKeyC = Arrays.copyOfRange(messageKeyCLarge, 8, 24);

                                if (!Arrays.equals(messageKey, messageKeyC)) {
                                    throw new IllegalStateException("Incorrect message key.");
                                }

                                ByteBuf decryptedBuf = alloc.buffer().writeBytes(decrypted);

                                long serverSalt = decryptedBuf.readLongLE();
                                log.trace("serverSalt: {}", serverSalt);
                                long sessionId = decryptedBuf.readLongLE();
                                log.trace("sessionId: {}", sessionId);
                                long messageId = decryptedBuf.readLongLE();
                                log.trace("messageId: {}", messageId);
                                int seqNo = decryptedBuf.readIntLE();
                                log.trace("seqNo: {}", seqNo);
                                int length = decryptedBuf.readIntLE();
                                log.trace("length: {}", length);

                                if (length % 4 != 0) {
                                    throw new IllegalStateException("Length of data isn't a multiple of four.");
                                }

                                session.updateTimeOffset(messageId >> 32);
                                session.setLastMessageId(messageId);

                                TlObject obj = TlDeserializer.deserialize(decryptedBuf.readBytes(length));

                                rpcReceiver.emitNext(obj, Sinks.EmitFailureHandler.FAIL_FAST);
                            })
                            .doOnDiscard(ByteBuf.class, ReferenceCountUtil::safeRelease)
                            .subscribe());

                    sink.success(session);
                }));
    }
}