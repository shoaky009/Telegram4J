package telegram4j.core.object.media;

import io.netty.buffer.ByteBuf;
import telegram4j.mtproto.util.TlEntityUtil;

import java.util.Objects;

public final class StrippedThumbnail implements Thumbnail {

    private final telegram4j.tl.PhotoStrippedSize data;

    public StrippedThumbnail(telegram4j.tl.PhotoStrippedSize data) {
        this.data = Objects.requireNonNull(data);
    }

    /**
     * Gets a single-char <a href="https://core.telegram.org/api/files#image-thumbnail-types">type</a> of thumbnail.
     *
     * @return The type of thumbnail, always is {@code 'i'}.
     */
    @Override
    public char getType() {
        return data.type().charAt(0);
    }

    public ByteBuf getStrippedContent() {
        return data.bytes();
    }

    public ByteBuf getContent() {
        return TlEntityUtil.expandInlineThumb(data.bytes());
    }

    @Override
    public String toString() {
        return "PhotoStrippedSize{" +
                "data=" + data +
                '}';
    }
}