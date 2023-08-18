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
package telegram4j.example.command;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import telegram4j.core.event.domain.message.SendMessageEvent;
import telegram4j.core.spec.ReplyToMessageSpec;
import telegram4j.core.spec.SendMessageSpec;

@TelegramCommand(command = "echo", description = "Repeat text.")
public class EchoCommand implements Command {
    @Override
    public Publisher<?> execute(SendMessageEvent event) {
        return Mono.justOrEmpty(event.getChat())
                .switchIfEmpty(event.getMessage().getChat())
                .flatMap(c -> {
                    String text = event.getMessage().getContent();
                    int spc = text.indexOf(' ');
                    return c.sendMessage(SendMessageSpec.builder()
                            .message(spc == -1 ? "Missing echo text." : text.substring(spc + 1))
                            .replyTo(ReplyToMessageSpec.of(event.getMessage()))
                            .build());
                });
    }
}
