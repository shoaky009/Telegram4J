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
package telegram4j.core.parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import telegram4j.tl.ImmutableMessageEntityBotCommand;
import telegram4j.tl.ImmutableMessageEntityEmail;
import telegram4j.tl.ImmutableMessageEntityHashtag;
import telegram4j.tl.MessageEntity;

import java.util.List;

import static telegram4j.core.util.parser.EntityParserSupport.scanUniform;

class UniformMessageEntityTest {

    @Test
    void test() {

        eq("", List.of());
        eq("send `/help@botbot` to get help!", List.of(ImmutableMessageEntityBotCommand.of(6, 12)));
        eq("example@domain.com send #hashtag", List.of(
                ImmutableMessageEntityEmail.of(0, 18),
                ImmutableMessageEntityHashtag.of(24, 8)));
        eq("#hashtag", List.of(ImmutableMessageEntityHashtag.of(0, 8)));
    }

    static void eq(String text, List<MessageEntity> expected) {
        var list = scanUniform(text);
        Assertions.assertIterableEquals(expected, list);
    }
}
