package telegram4j.core.object.chat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuples;
import telegram4j.core.MTProtoTelegramClient;
import telegram4j.core.auxiliary.AuxiliarySendAs;
import telegram4j.core.internal.AuxiliaryEntityFactory;
import telegram4j.core.internal.EntityFactory;
import telegram4j.core.object.Message;
import telegram4j.core.spec.ForwardMessagesSpec;
import telegram4j.core.spec.SendMessageSpec;
import telegram4j.core.util.Id;
import telegram4j.core.util.PeerId;
import telegram4j.core.util.parser.EntityParserSupport;
import telegram4j.mtproto.util.CryptoUtil;
import telegram4j.tl.InputPeer;
import telegram4j.tl.InputPeerEmpty;
import telegram4j.tl.messages.AffectedHistory;
import telegram4j.tl.request.messages.ForwardMessages;
import telegram4j.tl.request.messages.SendMedia;
import telegram4j.tl.request.messages.SendMessage;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static reactor.function.TupleUtils.function;
import static telegram4j.mtproto.util.TlEntityUtil.unmapEmpty;

/** This class provides default implementation of {@link Chat} methods. */
abstract class BaseChat implements Chat {

    protected final MTProtoTelegramClient client;

    protected BaseChat(MTProtoTelegramClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public MTProtoTelegramClient getClient() {
        return client;
    }

    @Override
    public abstract Id getId();

    @Override
    public abstract Type getType();

    @Override
    public abstract Optional<String> getAbout();

    // Interaction methods implementation

    @Override
    public Mono<Message> sendMessage(SendMessageSpec spec) {
        return Mono.defer(() -> {
            var parser = spec.parser()
                    .or(() -> client.getMtProtoResources().getDefaultEntityParser())
                    .map(m -> EntityParserSupport.parse(client, m.apply(spec.message().trim())))
                    .orElseGet(() -> Mono.just(Tuples.of(spec.message(), List.of())));

            var replyMarkup = Mono.justOrEmpty(spec.replyMarkup())
                    .flatMap(r -> r.asData(client));

            var sendAs = Mono.justOrEmpty(spec.sendAs())
                    .flatMap(client::resolvePeer)
                    .flatMap(p -> client.asInputPeer(p.getId()));

            return Mono.justOrEmpty(spec.media())
                    .flatMap(d -> d.asData(client))
                    .flatMap(media -> parser.map(function((txt, ent) -> SendMedia.builder()
                                    .media(media)
                                    .randomId(CryptoUtil.random.nextLong())
                                    .peer(client.asResolvedInputPeer(getId()))
                                    .flags(spec.flags().getValue())
                                    .replyToMsgId(spec.replyToMessageId().orElse(null))
                                    .message(txt)
                                    .entities(ent)
                                    .scheduleDate(spec.scheduleTimestamp()
                                            .map(Instant::getEpochSecond)
                                            .map(Math::toIntExact)
                                            .orElse(null))))
                            .flatMap(builder -> replyMarkup.doOnNext(builder::replyMarkup)
                                    .then(sendAs.doOnNext(builder::sendAs))
                                    .then(Mono.fromSupplier(builder::build)))
                            .flatMap(client.getServiceHolder().getChatService()::sendMedia))
                    .switchIfEmpty(parser.map(function((txt, ent) -> SendMessage.builder()
                                    .randomId(CryptoUtil.random.nextLong())
                                    .peer(client.asResolvedInputPeer(getId()))
                                    .flags(spec.flags().getValue())
                                    .replyToMsgId(spec.replyToMessageId().orElse(null))
                                    .message(txt)
                                    .entities(ent)
                                    .scheduleDate(spec.scheduleTimestamp()
                                            .map(Instant::getEpochSecond)
                                            .map(Math::toIntExact)
                                            .orElse(null))))
                            .flatMap(builder -> replyMarkup.doOnNext(builder::replyMarkup)
                                    .then(sendAs.doOnNext(builder::sendAs))
                                    .then(Mono.fromSupplier(builder::build)))
                            .flatMap(client.getServiceHolder().getChatService()::sendMessage))
                    .map(e -> EntityFactory.createMessage(client, e, getId()));
        });
    }

    @Override
    public Flux<Message> forwardMessages(ForwardMessagesSpec spec, PeerId toPeer) {
        return client.resolvePeer(toPeer)
                .flatMap(p -> client.asInputPeer(p.getId()))
                .zipWith(Mono.justOrEmpty(spec.sendAs())
                        .flatMap(client::resolvePeer)
                        .flatMap(p -> client.asInputPeer(p.getId()))
                        .defaultIfEmpty(InputPeerEmpty.instance()))
                .flatMapMany(function((toPeerResend, sendAs) -> client.getServiceHolder().getChatService()
                        .forwardMessages(ForwardMessages.builder()
                                .id(spec.ids())
                                .randomId(CryptoUtil.random.longs(spec.ids().size())
                                        .boxed()
                                        .collect(Collectors.toList()))
                                .flags(spec.flags().getValue())
                                .fromPeer(client.asResolvedInputPeer(getId()))
                                .toPeer(toPeerResend)
                                .sendAs(unmapEmpty(sendAs))
                                .scheduleDate(spec.scheduleTimestamp()
                                        .map(Instant::getEpochSecond)
                                        .map(Math::toIntExact)
                                        .orElse(null))
                                .build())))
                        .flatMap(e -> client.asInputPeer(Id.of(e.peerId()))
                                .map(p -> EntityFactory.createMessage(client, e,
                                        Id.of(p, client.getSelfId()))));
    }

    @Override
    public Mono<AuxiliarySendAs> getSendAs() {
        InputPeer peer = client.asResolvedInputPeer(getId());

        return client.getServiceHolder().getChatService().getSendAs(peer)
                .map(s -> AuxiliaryEntityFactory.createSendAs(client, s));
    }

    @Override
    public Mono<AffectedHistory> unpinAllMessages() {
        InputPeer peer = client.asResolvedInputPeer(getId());

        return client.getServiceHolder().getChatService().unpinAllMessages(peer);
    }

    @Override
    public final boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseChat)) return false;
        BaseChat that = (BaseChat) o;
        return getId().equals(that.getId());
    }

    @Override
    public final int hashCode() {
        return getId().hashCode();
    }
}
