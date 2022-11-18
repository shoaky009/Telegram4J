package telegram4j.mtproto.store.object;

import reactor.util.annotation.Nullable;
import telegram4j.tl.BaseUser;
import telegram4j.tl.api.TlObject;

import java.util.List;

public class ChatData<M extends TlObject, F extends TlObject> extends PeerData<M, F> {
    // channel/chat bots and participants (only for group chats)
    public final List<BaseUser> users;

    public ChatData(M chatMin, @Nullable F chatFull, List<BaseUser> users) {
        super(chatMin, chatFull);
        this.users = users;
    }
}
