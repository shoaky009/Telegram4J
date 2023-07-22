package telegram4j.core.object;

import telegram4j.core.MTProtoTelegramClient;
import telegram4j.core.internal.MappingUtil;
import telegram4j.mtproto.file.Context;
import telegram4j.tl.BaseDocument;
import telegram4j.tl.DocumentAttributeVideo;
import telegram4j.tl.WebDocument;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Inferred from document attributes type of video and gif documents. */
public final class Video extends Document {

    private final telegram4j.tl.DocumentAttributeVideo videoData;
    private final boolean hasStickers;
    private final boolean gif;

    public Video(MTProtoTelegramClient client, BaseDocument data,
                 String fileName, Context context, DocumentAttributeVideo videoData,
                 boolean hasStickers, boolean gif) {
        super(client, data, fileName, context);
        this.videoData = Objects.requireNonNull(videoData);
        this.hasStickers = hasStickers;
        this.gif = gif;
    }

    public Video(MTProtoTelegramClient client, WebDocument data,
                 String fileName, Context context, DocumentAttributeVideo videoData,
                 boolean hasStickers, boolean gif) {
        super(client, data, fileName, context);
        this.videoData = Objects.requireNonNull(videoData);
        this.hasStickers = hasStickers;
        this.gif = gif;
    }

    /**
     * Gets whether video document contains stickers.
     *
     * @return Whether video document contains stickers.
     */
    public boolean isHasStickers() {
        return hasStickers;
    }

    /**
     * Gets type of video document.
     *
     * @return The type of video.
     */
    public Type getType() {
        return gif ? Type.GIF : videoData.roundMessage() ? Type.ROUND : Type.REGULAR;
    }

    /**
     * Gets whether video supports streaming.
     *
     * @return Whether video supports streaming.
     */
    public boolean isSupportsStreaming() {
        return videoData.supportsStreaming();
    }

    /**
     * Gets duration of video document.
     *
     * @return The duration of video document.
     */
    public Duration getDuration() {
        return MappingUtil.durationFromSeconds(videoData.duration());
    }

    public Optional<Integer> getPreloadPrefixSize() {
        return Optional.ofNullable(videoData.preloadPrefixSize());
    }

    /**
     * Gets width of video document.
     *
     * @return The width of video document.
     */
    public int getWidth() {
        return videoData.w();
    }

    /**
     * Gets height of video document.
     *
     * @return The height of video document.
     */
    public int getHeight() {
        return videoData.h();
    }

    @Override
    public String toString() {
        return "Video{" +
                "data=" + data +
                ", fileReferenceId=" + fileReferenceId +
                '}';
    }

    /** Types of video documents. */
    public enum Type {
        /** Represents regular video files. */
        REGULAR,

        /** Represents video-rounds files. */
        ROUND,

        /** Represents gif files. */
        GIF
    }
}
