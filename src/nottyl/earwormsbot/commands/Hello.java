package nottyl.earwormsbot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import nottyl.earwormsbot.ICommand;


public class Hello implements ICommand {

    @Override
    public String name() {
        return "hello";
    }

    @Override
    public void execute(MessageCreateEvent event) {
        event.getMessage()
                .getChannel().block()
                .createMessage("👋 | Hello to you too!").block();
    }
}
