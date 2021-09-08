package telegram4j.json.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import telegram4j.json.InputMediaData;
import telegram4j.json.ReplyMarkupData;
import telegram4j.json.api.ChatId;
import telegram4j.json.api.Id;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableMessageEditMediaRequest.class)
@JsonDeserialize(as = ImmutableMessageEditMediaRequest.class)
public interface MessageEditMediaRequest {

    static ImmutableMessageEditMediaRequest.Builder builder() {
        return ImmutableMessageEditMediaRequest.builder();
    }

    @JsonProperty("chat_id")
    Optional<ChatId> chatId();

    @JsonProperty("message_id")
    Optional<Id> messageId();

    @JsonProperty("inline_message_id")
    Optional<String> inlineMessageId();

    InputMediaData media();

    @JsonProperty("reply_markup")
    Optional<ReplyMarkupData> replyMarkup();
}