package telegram4j.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import telegram4j.json.MessageEntityData;
import telegram4j.json.ParseMode;
import telegram4j.json.ReplyMarkupData;
import telegram4j.json.api.ChatId;
import telegram4j.json.api.Id;

import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableMessageCreate.class)
@JsonDeserialize(as = ImmutableMessageCreate.class)
public interface MessageCreate {

    static ImmutableMessageCreate.Builder builder() {
        return ImmutableMessageCreate.builder();
    }

    @JsonProperty("chat_id")
    ChatId chatId();

    String text();

    @JsonProperty("parse_mode")
    Optional<ParseMode> parseMode();

    Optional<List<MessageEntityData>> entities();

    @JsonProperty("disable_web_preview")
    Optional<Boolean> disableWebPreview();

    @JsonProperty("disable_notification")
    Optional<Boolean> disableNotification();

    @JsonProperty("reply_to_message_id")
    Optional<Id> replyToMessageId();

    @JsonProperty("allow_sending_without_reply")
    Optional<Boolean> allowSendingWithoutReply();

    @JsonProperty("reply_markup")
    Optional<ReplyMarkupData> replyMarkup();
}
