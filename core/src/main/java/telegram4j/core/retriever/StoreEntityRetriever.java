package telegram4j.core.retriever;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;
import telegram4j.core.MTProtoTelegramClient;
import telegram4j.core.auxiliary.AuxiliaryMessages;
import telegram4j.core.internal.AuxiliaryEntityFactory;
import telegram4j.core.internal.EntityFactory;
import telegram4j.core.object.PeerEntity;
import telegram4j.core.object.User;
import telegram4j.core.object.chat.Chat;
import telegram4j.core.object.chat.ChatParticipant;
import telegram4j.core.util.Id;
import telegram4j.core.util.PeerId;
import telegram4j.mtproto.store.StoreLayout;
import telegram4j.mtproto.util.TlEntityUtil;
import telegram4j.tl.InputMessage;
import telegram4j.tl.PeerChannel;
import telegram4j.tl.PeerUser;

/** Implementation of {@code EntityRetriever} which uses {@link StoreLayout storage}. */
public class StoreEntityRetriever implements EntityRetriever {

    private final MTProtoTelegramClient client;
    private final StoreLayout storeLayout;
    private final boolean retrieveSelfUserForDMs;

    public StoreEntityRetriever(MTProtoTelegramClient client) {
        this(client, true);
    }

    public StoreEntityRetriever(MTProtoTelegramClient client, boolean retrieveSelfUserForDMs) {
        this(client, client.getMtProtoResources().getStoreLayout(), retrieveSelfUserForDMs);
    }

    private StoreEntityRetriever(MTProtoTelegramClient client, StoreLayout storeLayout, boolean retrieveSelfUserForDMs) {
        this.client = client;
        this.storeLayout = storeLayout;
        this.retrieveSelfUserForDMs = retrieveSelfUserForDMs;
    }

    public StoreEntityRetriever withRetrieveSelfUserForDMs(boolean state) {
        if (retrieveSelfUserForDMs == state) return this;
        return new StoreEntityRetriever(client, storeLayout, state);
    }

    @Override
    public Mono<PeerEntity> resolvePeer(PeerId peerId) {
        Mono<PeerEntity> resolveById = Mono.justOrEmpty(peerId.asId())
                .flatMap(id -> {
                    switch (id.getType()) {
                        case CHAT:
                        case CHANNEL: return getChatMinById(id);
                        case USER: return getUserMinById(id);
                        default: return Mono.error(new IllegalStateException());
                    }
                });

        return Mono.justOrEmpty(peerId.asUsername())
                .flatMap(storeLayout::resolvePeer)
                .flatMap(p -> {
                    switch (p.peer().identifier()) {
                        case PeerChannel.ID: return Mono.justOrEmpty(EntityFactory.createChat(client, p.chats().get(0), null));
                        case PeerUser.ID: return Mono.justOrEmpty(EntityFactory.createUser(client, p.users().get(0)));
                        default: return Mono.error(new IllegalStateException("Unknown Peer type: " + p.peer()));
                    }
                })
                .switchIfEmpty(resolveById);
    }

    @Override
    public Mono<User> getUserById(Id userId) {
        if (userId.getType() != Id.Type.USER) {
            return Mono.error(new IllegalArgumentException("Incorrect id type, expected: "
                    + Id.Type.USER + ", but given: " + userId.getType()));
        }

        return storeLayout.getUserById(userId.asLong())
                .map(u -> new User(client, u.minData, u.fullData));
    }

    @Override
    public Mono<User> getUserMinById(Id userId) {
        if (userId.getType() != Id.Type.USER) {
            return Mono.error(new IllegalArgumentException("Incorrect id type, expected: "
                    + Id.Type.USER + ", but given: " + userId.getType()));
        }

        return storeLayout.getUserMinById(userId.asLong())
                .map(u -> new User(client, u, null));
    }

    @Override
    public Mono<User> getUserFullById(Id userId) {
        if (userId.getType() != Id.Type.USER) {
            return Mono.error(new IllegalArgumentException("Incorrect id type, expected: "
                    + Id.Type.USER + ", but given: " + userId.getType()));
        }

        return storeLayout.getUserFullById(userId.asLong())
                .mapNotNull(u -> EntityFactory.createUser(client, u));
    }

    @Override
    public Mono<Chat> getChatById(Id chatId) {
        return Mono.defer(() -> {
            switch (chatId.getType()) {
                case CHAT: return storeLayout.getChatById(chatId.asLong())
                        .mapNotNull(c -> EntityFactory.createChat(client, c, null));
                case CHANNEL: return storeLayout.getChannelById(chatId.asLong())
                        .mapNotNull(c -> EntityFactory.createChat(client, c, null));
                case USER: return storeLayout.getUserById(chatId.asLong())
                        .flatMap(p -> {
                            var retrieveSelf = retrieveSelfUserForDMs
                                    ? getUserById(client.getSelfId())
                                    : Mono.<User>empty();
                            return retrieveSelf
                                    .map(u -> EntityFactory.createChat(client, p, u))
                                    .switchIfEmpty(Mono.fromSupplier(() -> EntityFactory.createChat(client, p, null)));
                        });
                default: return Mono.error(new IllegalStateException());
            }
        });
    }

    @Override
    public Mono<Chat> getChatMinById(Id chatId) {
        return Mono.defer(() -> {
                    switch (chatId.getType()) {
                        case CHAT: return storeLayout.getChatMinById(chatId.asLong())
                                .mapNotNull(c -> EntityFactory.createChat(client, c, null));
                        case CHANNEL: return storeLayout.getChannelMinById(chatId.asLong())
                                .mapNotNull(c -> EntityFactory.createChat(client, c, null));
                        case USER: return storeLayout.getUserMinById(chatId.asLong())
                                .flatMap(p -> {
                                    var retrieveSelf = retrieveSelfUserForDMs
                                            ? getUserById(client.getSelfId())
                                            : Mono.<User>empty();
                                    return retrieveSelf
                                            .mapNotNull(u -> EntityFactory.createChat(client, p, u))
                                            .switchIfEmpty(Mono.fromSupplier(() -> EntityFactory.createChat(client, p, null)));
                                });
                        default: return Mono.error(new IllegalStateException());
                    }
                });
    }

    @Override
    public Mono<Chat> getChatFullById(Id chatId) {
        return Mono.defer(() -> {
                    switch (chatId.getType()) {
                        case CHAT: return storeLayout.getChatFullById(chatId.asLong())
                                .mapNotNull(c -> EntityFactory.createChat(client, c, null));
                        case CHANNEL: return storeLayout.getChannelFullById(chatId.asLong())
                                .mapNotNull(c -> EntityFactory.createChat(client, c, null));
                        case USER: return storeLayout.getUserFullById(chatId.asLong())
                                .flatMap(p -> {
                                    var retrieveSelf = retrieveSelfUserForDMs
                                            ? getUserById(client.getSelfId())
                                            : Mono.<User>empty();
                                    return retrieveSelf
                                            .mapNotNull(u -> EntityFactory.createChat(client, p, u))
                                            .switchIfEmpty(Mono.fromSupplier(() -> EntityFactory.createChat(client, p, null)));
                                });
                        default: return Mono.error(new IllegalStateException());
                    }
                });
    }

    @Override
    public Mono<ChatParticipant> getParticipantById(Id chatId, Id peerId) {
        return Mono.defer(() -> {
            switch (chatId.getType()) {
                case CHAT:
                    if (peerId.getType() != Id.Type.USER) {
                        return Mono.error(new IllegalArgumentException("Incorrect id type, expected: USER, " +
                                "but given: " + chatId.getType()));
                    }

                    return storeLayout.getChatParticipantById(chatId.asLong(), peerId.asLong())
                            .map(r -> new ChatParticipant(client, r.getUser()
                                    .map(u -> EntityFactory.createUser(client, u))
                                    .orElse(null), r.getParticipant(), chatId));
                case CHANNEL:
                    return storeLayout.getChannelParticipantById(chatId.asLong(), peerId.asPeer())
                            .map(p -> EntityFactory.createChannelParticipant(client, p, chatId, peerId));
                default:
                    return Mono.error(new IllegalArgumentException("Incorrect id type, expected: CHANNEL or " +
                            "CHAT, but given: " + chatId.getType()));
            }
        });
    }

    @Override
    public Flux<ChatParticipant> getParticipants(Id chatId) {
        return Flux.defer(() -> {
            switch (chatId.getType()) {
                case CHAT:
                    return storeLayout.getChatParticipants(chatId.asLong())
                            .map(r -> new ChatParticipant(client, r.getUser()
                                    .map(u -> EntityFactory.createUser(client, u))
                                    .orElse(null), r.getParticipant(), chatId));
                case CHANNEL:
                    return storeLayout.getChannelParticipants(chatId.asLong())
                            .map(p -> EntityFactory.createChannelParticipant(client, p, chatId,
                                    Id.of(TlEntityUtil.getUserId(p.participant()))));
                default:
                    return Mono.error(new IllegalArgumentException("Incorrect id type, expected: CHANNEL or " +
                            "CHAT, but given: " + chatId.getType()));
            }
        });
    }

    @Override
    public Mono<AuxiliaryMessages> getMessages(@Nullable Id chatId, Iterable<? extends InputMessage> messageIds) {
        return Mono.defer(() -> {
            if (chatId == null || chatId.getType() != Id.Type.CHANNEL) {
                return storeLayout.getMessages(messageIds);
            }
            return storeLayout.getMessages(chatId.asLong(), messageIds);
        })
        .map(d -> AuxiliaryEntityFactory.createMessages(client, d));
    }
}
