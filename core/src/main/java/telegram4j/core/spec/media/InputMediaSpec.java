/*
 * Copyright 2023 Telegram4J
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package telegram4j.core.spec.media;

import telegram4j.core.internal.MonoSpec;
import telegram4j.tl.InputMedia;

// TODO: Implement spec for inputMediaGame
public sealed interface InputMediaSpec extends MonoSpec<InputMedia>
        permits InputMediaContactSpec, InputMediaDiceSpec, InputMediaDocumentSpec,
                InputMediaGeoLiveSpec, InputMediaGeoPointSpec, InputMediaPhotoSpec,
                InputMediaPollSpec, InputMediaUploadedDocumentSpec, InputMediaUploadedPhotoSpec,
                InputMediaVenueSpec {
}
