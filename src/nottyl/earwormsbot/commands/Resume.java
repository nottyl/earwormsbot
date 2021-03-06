package nottyl.earwormsbot.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import nottyl.earwormsbot.ICommand;
import nottyl.earwormsbot.Main;
import nottyl.earwormsbot.lavaplayer.MusicManager;

public class Resume implements ICommand {

    @Override
    public String name() {
        return "resume";
    }

    @Override
    public void execute(MessageCreateEvent event) {
        event.getMessage().getChannel()
                .subscribe(ch -> {
                    final MusicManager mgr = Main.guildMusicManager.getMusicManager(event);
                    mgr.resume();
                    ch.createMessage("⏯ | The music is resumed.").subscribe();
                });
    }
}
