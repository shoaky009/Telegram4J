package telegram4j.mtproto.file;

import org.junit.jupiter.api.Test;
import telegram4j.mtproto.util.CryptoUtil;
import telegram4j.tl.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static telegram4j.mtproto.file.FileReferenceId.*;

class FileReferenceIdTest {

    @Test
    void all() {
        var expChatPhoto = ofChatPhoto(ImmutableBaseUserProfilePhoto.of(1337, 2),
                PhotoSizeType.CHAT_PHOTO_BIG, -1, InputPeerSelf.instance());
        var expMinPhoto = ofChatPhoto(BasePhoto.builder()
                .id(1337)
                .accessHash(-1111)
                .fileReference(CryptoUtil.random.generateSeed(8))
                .date(Math.toIntExact(System.currentTimeMillis() / 1000))
                .sizes(List.of(BasePhotoSize.builder()
                        .type("i")
                        .w(100)
                        .h(100)
                        .size(-1)
                        .build()))
                .dcId(2)
                .build(), 1337, ImmutableInputPeerUser.of(1234, -4321));
        var expDocument = ofDocument(BaseDocument.builder()
                .id(1337)
                .accessHash(-1111)
                .fileReference(CryptoUtil.random.generateSeed(8))
                .date(Math.toIntExact(System.currentTimeMillis() / 1000))
                .thumbs(List.of(BasePhotoSize.builder()
                        .type("i")
                        .w(100)
                        .h(100)
                        .size(-1)
                        .build()))
                .dcId(2)
                // ignored fields
                .mimeType("")
                .size(-1)
                .attributes(List.of())
                .build(), 1, InputPeerSelf.instance());
        var expPhoto = ofPhoto(BasePhoto.builder()
                .id(1337)
                .accessHash(-1111)
                .fileReference(CryptoUtil.random.generateSeed(8))
                .date(Math.toIntExact(System.currentTimeMillis() / 1000))
                .sizes(List.of(BasePhotoSize.builder()
                        .type("i")
                        .w(100)
                        .h(100)
                        .size(-1)
                        .build()))
                .dcId(2)
                .build(), 1, InputPeerSelf.instance());
        var expStickerSet = ofStickerSet(ImmutableInputStickerSetID.of(1337, -1111), 2);

        var actChatPhoto = serialize(expChatPhoto);
        var actMinPhoto = serialize(expMinPhoto);
        var actDocument = serialize(expDocument);
        var actPhoto = serialize(expPhoto);
        var actStickerSet = serialize(expStickerSet);

        assertEquals(expChatPhoto, actChatPhoto);
        assertEquals(expMinPhoto, actMinPhoto);
        assertEquals(expDocument, actDocument);
        assertEquals(expPhoto, actPhoto);
        assertEquals(expStickerSet, actStickerSet);
    }

    static FileReferenceId serialize(FileReferenceId ref) {
        return deserialize(ref.serialize());
    }
}