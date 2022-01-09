package telegram4j.mtproto.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import telegram4j.mtproto.MTProtoClient;
import telegram4j.mtproto.store.StoreLayout;
import telegram4j.tl.*;
import telegram4j.tl.contacts.*;
import telegram4j.tl.help.UserInfo;
import telegram4j.tl.photos.Photo;
import telegram4j.tl.photos.Photos;
import telegram4j.tl.request.contacts.*;
import telegram4j.tl.request.help.EditUserInfo;
import telegram4j.tl.request.help.ImmutableGetUserInfo;
import telegram4j.tl.request.photos.*;
import telegram4j.tl.request.users.GetUsers;
import telegram4j.tl.request.users.ImmutableGetFullUser;
import telegram4j.tl.request.users.SetSecureValueErrors;
import telegram4j.tl.users.UserFull;

import java.util.function.Function;

public class UserService extends RpcService {

    public UserService(MTProtoClient client, StoreLayout storeLayout) {
        super(client, storeLayout);
    }

    public Flux<User> getUsers(Iterable<? extends InputUser> userIds) {
        return client.sendAwait(GetUsers.builder().addAllId(userIds).build())
                .flatMapIterable(Function.identity())
                .flatMap(u -> storeLayout.onUserUpdate(u)
                        .thenReturn(u));
    }

    public Mono<UserFull> getFullUser(InputUser id) {
        return client.sendAwait(ImmutableGetFullUser.of(id))
                .flatMap(u -> storeLayout.onUserUpdate(u).thenReturn(u));
    }

    public Mono<Boolean> setSecureValueErrors(InputUser id, Iterable<? extends SecureValueError> errors) {
        return client.sendAwait(SetSecureValueErrors.builder()
                .id(id)
                .errors(errors)
                .build());
    }

    // They are methods form other RPC service, but user-related
    // Methods from ContactsService

    public Mono<ResolvedPeer> resolveUsername(String username) {
        return Mono.defer(() -> {
            String corrected = username.toLowerCase().trim()
                    .replace(".", "")
                    .replace("@", "");

            return client.sendAwait(ImmutableResolveUsername.of(corrected));
        });
    }

    public Flux<Integer> getContactsIds(long hash) {
        return client.sendAwait(ImmutableGetContactIDs.of(hash))
                .flatMapIterable(Function.identity());
    }

    public Flux<ContactStatus> getStatuses() {
        return client.sendAwait(GetStatuses.instance())
                .flatMapIterable(Function.identity());
    }

    public Mono<Contacts> getContacts(long hash) {
        return client.sendAwait(ImmutableGetContacts.of(hash));
    }

    public Mono<ImportedContacts> importContacts(Iterable<? extends InputContact> contacts) {
        return client.sendAwait(ImportContacts.builder().contacts(contacts).build());
    }

    // TODO: check updates type
    public Mono<Updates> deleteContacts(Iterable<? extends InputUser> ids) {
        return client.sendAwait(DeleteContacts.builder().id(ids).build());
    }

    public Mono<Boolean> block(InputPeer peer) {
        return client.sendAwait(ImmutableBlock.of(peer));
    }

    public Mono<Boolean> unblock(InputPeer peer) {
        return client.sendAwait(ImmutableUnblock.of(peer));
    }

    public Mono<Blocked> getBlocked(int offset, int limit) {
        return client.sendAwait(ImmutableGetBlocked.of(offset, limit));
    }

    public Mono<Found> search(String query, int limit) {
        return client.sendAwait(ImmutableSearch.of(query, limit));
    }

    public Mono<TopPeers> getTopPeers(GetTopPeers request) {
        return client.sendAwait(request);
    }

    public Mono<Boolean> resetTopPeerRating(TopPeerCategory category, InputPeer peer) {
        return client.sendAwait(ImmutableResetTopPeerRating.of(category, peer));
    }

    public Mono<Boolean> resetSaves() {
        return client.sendAwait(ResetSaved.instance());
    }

    public Flux<SavedContact> getSaved() {
        return client.sendAwait(GetSaved.instance())
                .flatMapIterable(Function.identity());
    }

    public Mono<Boolean> toggleTopPeers(boolean enabled) {
        return client.sendAwait(ImmutableToggleTopPeers.of(enabled));
    }

    // TODO: check updates type
    public Mono<Updates> addContact(AddContact request) {
        return client.sendAwait(request);
    }

    public Mono<Updates> acceptContact(InputUser id) {
        return client.sendAwait(ImmutableAcceptContact.of(id));
    }

    public Mono<Updates> getLocated(GetLocated request) {
        return client.sendAwait(request);
    }

    public Mono<Updates> blockFromReplies(BlockFromReplies request) {
        return client.sendAwait(request);
    }

    // Methods from HelpService

    public Mono<UserInfo> getUserInfo(InputUser id) {
        return client.sendAwait(ImmutableGetUserInfo.of(id))
                .flatMap(u -> storeLayout.onUserInfoUpdate(u)
                        .thenReturn(u));
    }

    public Mono<UserInfo> editUserInfo(InputUser id, String message, Iterable<? extends MessageEntity> entities) {
        return client.sendAwait(EditUserInfo.builder()
                        .userId(id)
                        .message(message)
                        .entities(entities)
                        .build())
                .flatMap(u -> storeLayout.onUserInfoUpdate(u)
                        .thenReturn(u));
    }

    // Methods from PhotoService

    public Mono<Photo> updateProfilePhoto(InputPhoto photo) {
        return client.sendAwait(ImmutableUpdateProfilePhoto.of(photo));
    }

    public Mono<Photo> uploadProfilePhoto(UploadProfilePhoto request) {
        return client.sendAwait(request);
    }

    public Flux<Long> deletePhotos(Iterable<? extends InputPhoto> photos) {
        return client.sendAwait(DeletePhotos.builder().id(photos).build())
                .flatMapIterable(Function.identity());
    }

    public Mono<Photos> getUserPhotos(InputUser id, int offset, long maxId, int limit) {
        return client.sendAwait(ImmutableGetUserPhotos.of(id, offset, maxId, limit));
    }
}