package telegram4j.mtproto.store;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import telegram4j.mtproto.DataCenter;
import telegram4j.mtproto.auth.AuthorizationKeyHolder;
import telegram4j.tl.*;
import telegram4j.tl.channels.BaseChannelParticipants;
import telegram4j.tl.channels.ChannelParticipant;
import telegram4j.tl.contacts.ResolvedPeer;
import telegram4j.tl.messages.ChatFull;
import telegram4j.tl.messages.Messages;
import telegram4j.tl.updates.State;
import telegram4j.tl.users.UserFull;

/**
 * Storage interface for interacting with tl entities and session information.
 * <p>Due to message ids specific differ from user to user,
 * the storage cannot be used by more than one user.
 */
public interface StoreLayout {

    /**
     * Retrieve main dc to which store assigned.
     *
     * @return A {@link Mono} emitting on successful completion the {@link DataCenter dc} id
     */
    Mono<DataCenter> getDataCenter();

    /**
     * Retrieve local updates state.
     *
     * @return A {@link Mono} emitting on successful completion the {@link State}
     * object with info about local updates state.
     */
    Mono<State> getCurrentState();

    /**
     * Retrieve self user id.
     *
     * @return A {@link Mono} emitting on successful completion a self user id.
     */
    Mono<Long> getSelfId();

    /**
     * Search channel or user by the specified username
     * and compute container with minimal found info.
     *
     * @implSpec the implementation must support <b>me</b> and <b>self</b>
     * aliases to retrieve information about itself.
     *
     * @param username The username of channel or user.
     * @return A {@link Mono} emitting on successful completion
     * a container with minimal information about found peer.
     */
    Mono<ResolvedPeer> resolvePeer(String username);

    /**
     * Resolve input peer from specified peer id.
     * This method can be used to get access hash
     * or to check if id equals self id.
     *
     * @param peerId The id of peer.
     * @return A {@link Mono} emitting on successful completion
     * the {@link InputPeer} object, but not {@link InputPeerEmpty}.
     */
    Mono<InputPeer> resolvePeer(Peer peerId);

    /**
     * Resolve input user id from specified user id.
     * This method can be used to get access hash
     * or to check if id equals self id.
     *
     * @param userId The id of user.
     * @return A {@link Mono} emitting on successful completion
     * the {@link BaseInputUser} with access hash
     * or {@link InputUserFromMessage} if user is min
     * and the storage isn't associated with bot.
     */
    Mono<InputUser> resolveUser(long userId);

    /**
     * Resolve input channel id from specified channel id.
     * This method can be used to get access hash.
     *
     * @param channelId The id of channel.
     * @return A {@link Mono} emitting on successful completion
     * the {@link BaseInputChannel} with access hash
     * or {@link InputChannelFromMessage} if channel is min
     * and the storage isn't associated with bot.
     */
    Mono<InputChannel> resolveChannel(long channelId);

    /**
     * Check existence of message.
     * <p>Currently used only in updates handling.
     *
     * @param message The ordinal or service message to check.
     * @return A {@link Mono} emitting on successful completion {@code true} if message exists.
     */
    Mono<Boolean> existMessage(BaseMessageFields message);

    /**
     * Retrieve user/group chat's messages with auxiliary data by given ids.
     *
     * @implNote Implementation can emit {@link UnsupportedOperationException} for types of the {@code InputMessage}
     * which can't be handled.
     *
     * @param messageIds An iterable of message id elements.
     * @return A {@link Mono} emitting on successful completion
     * the container with found messages and auxiliary data.
     */
    Mono<Messages> getMessages(Iterable<? extends InputMessage> messageIds);

    /**
     * Retrieve channel's messages with auxiliary data by given ids.
     *
     * @implNote Implementation can emit {@link UnsupportedOperationException} for types of the {@code InputMessage}
     * which can't be handled.
     *
     * @param channelId The id of channel.
     * @param messageIds An iterable of message id elements.
     * @return A {@link Mono} emitting on successful completion
     * the container with found messages and auxiliary data.
     */
    Mono<Messages> getMessages(long channelId, Iterable<? extends InputMessage> messageIds);

    /**
     * Retrieve minimal chat information by specified id.
     *
     * @param chatId The id of chat.
     * @return A {@link Mono} emitting on successful completion
     * the {@link BaseChat} object.
     */
    Mono<BaseChat> getChatMinById(long chatId);

    /**
     * Retrieve detailed chat information by specified id.
     *
     * @param chatId The id of chat.
     * @return A {@link Mono} emitting on successful completion
     * the {@link ChatFull} container with detailed and minimal information about chat.
     */
    Mono<ChatFull> getChatFullById(long chatId);

    /**
     * Retrieve minimal channel information by specified id.
     *
     * @param channelId The id of chat.
     * @return A {@link Mono} emitting on successful completion
     * the {@link Channel} object.
     */
    Mono<Channel> getChannelMinById(long channelId);

    /**
     * Retrieve detailed channel information by specified id.
     *
     * @param channelId The id of channel.
     * @return A {@link Mono} emitting on successful completion
     * the {@link ChatFull} container with detailed and minimal information about channel.
     */
    Mono<ChatFull> getChannelFullById(long channelId);

    /**
     * Retrieve minimal user information by specified id.
     *
     * @param userId The id of user.
     * @return A {@link Mono} emitting on successful completion the {@link BaseUser} object.
     */
    Mono<BaseUser> getUserMinById(long userId);

    /**
     * Retrieve detailed user information by specified id.
     *
     * @param userId The id of user.
     * @return A {@link Mono} emitting on successful completion
     * the {@link UserFull} container with detailed and minimal information about user.
     */
    Mono<UserFull> getUserFullById(long userId);

    /**
     * Retrieve channel participant by specified channel id and peer id.
     *
     * @param channelId The id of channel.
     * @param peerId The id of participant.
     * @return A {@link Mono} emitting on successful completion
     * the {@link ChannelParticipant} container with channel participant and peer info.
     */
    Mono<ChannelParticipant> getChannelParticipantById(long channelId, Peer peerId);

    /**
     * Retrieve channel participants by specified channel id.
     *
     * @param channelId The id of channel.
     * @return A {@link Mono} emitting on successful completion
     * the {@link ChannelParticipant} containers with channel participant and peer info.
     */
    Flux<ChannelParticipant> getChannelParticipants(long channelId);

    /**
     * Retrieve group chat participant by specified chat id and user id.
     *
     * @param chatId The id of group chat.
     * @param userId The id of participant.
     * @return A {@link Mono} emitting on successful completion
     * the {@link ResolvedChatParticipant participant} with optional user info.
     */
    Mono<ResolvedChatParticipant> getChatParticipantById(long chatId, long userId);

    /**
     * Retrieve group chat participants by specified chat id.
     *
     * @param chatId The id of group chat.
     * @return A {@link Flux} which continually emits
     * the {@link ResolvedChatParticipant} containers with chat participant and their user info.
     */
    Flux<ResolvedChatParticipant> getChatParticipants(long chatId);

    /**
     * Retrieve auth key holder, associated with specified dc.
     *
     * @param dc The id of datacenter.
     * @return A {@link Mono} emitting on successful completion
     * the {@link AuthorizationKeyHolder} object with auth key and id.
     */
    Mono<AuthorizationKeyHolder> getAuthorizationKey(DataCenter dc);

    // message updates

    Mono<Void> onNewMessage(Message update);

    Mono<Message> onEditMessage(Message update);

    Mono<ResolvedDeletedMessages> onDeleteMessages(UpdateDeleteMessagesFields update);

    Mono<Void> onUpdatePinnedMessages(UpdatePinnedMessagesFields payload);

    // chat updates

    Mono<Void> onChatParticipant(UpdateChatParticipant payload);

    Mono<Void> onChannelParticipant(UpdateChannelParticipant payload);

    Mono<Void> onChatParticipants(ChatParticipants payload);

    // not an update-related methods

    Mono<Void> updateDataCenter(DataCenter dc);

    Mono<Void> updateState(State state);

    Mono<Void> updateAuthorizationKey(DataCenter dc, AuthorizationKeyHolder authKey);

    Mono<Void> updateChannelPts(long channelId, int pts);

    // common request methods

    Mono<Void> onContacts(Iterable<? extends Chat> chats, Iterable<? extends User> users);

    Mono<Void> onUserUpdate(telegram4j.tl.users.UserFull payload);

    Mono<Void> onChatUpdate(telegram4j.tl.messages.ChatFull payload);

    Mono<Void> onChannelParticipants(long channelId, BaseChannelParticipants payload);

    Mono<Void> onChannelParticipant(long channelId, ChannelParticipant payload);

    Mono<Void> onMessages(Messages payload);
}
