package nottyl.earwormsbot.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.voice.AudioProvider;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages playerManager, player, and audioProvider of a specific guild.
 */

public class MusicManager extends AudioEventAdapter{

    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final AudioProvider provider;
    private final MessageCreateEvent event;


    private final TrackScheduler scheduler;

    public MusicManager(MessageCreateEvent event) {
        playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        AudioSourceManagers.registerRemoteSources(playerManager);
        player = playerManager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
        provider = new LavaPlayerAudioProvider(player);
        this.event = event;
    }



    public AudioProvider getProvider() {
        return provider;
    }

    /**
     * @param trackUrl    query or URL of a song
     *  interrupt the song currently playing
     */

    public void play(String trackUrl) {
        playerManager.loadItemOrdered(event, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                boolean isPlaying = !player.startTrack(track, true);
                if (!isPlaying) {
                    nowPlaying(track);
                }
                scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                event.getMessage().getChannel()
                        .flatMap(replyChannel -> replyChannel.createMessage("🎛 | Playlist loaded..."))
                        .subscribe();
                AudioTrack firstTrack = playlist.getTracks().get(0);
                boolean isPlaying = player.startTrack(firstTrack, true);
                if (!isPlaying) {
                    nowPlaying(firstTrack);
                }
                scheduler.queue(tracks.remove(0));
                scheduler.queue.offer((AudioTrack) tracks);
            }

            @Override
            public void noMatches() {
                event.getMessage().getChannel()
                        .flatMap(replyChannel -> replyChannel.createMessage("🎛 | No matches..."))
                        .subscribe();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.getMessage().getChannel()
                        .flatMap(replyChannel -> replyChannel.createMessage("🎛 | Something went wrong... Please try again"))
                        .subscribe();
            }
        });

    }


    public void pause() {
        player.setPaused(true);
    }

    public void resume() {
        player.setPaused(false);
    }

    public void stop() {
        player.stopTrack();
    }

    public void clear(){scheduler.clear();}

    public void currentSong() {
        AudioTrack track = player.getPlayingTrack();
        nowPlaying(track);
    }

    public void nextTrack(){
        scheduler.next();
        currentSong();
    }

    public void nowPlaying(AudioTrack track) {
        if (track != null) {
            event.getMessage().getChannel()
                    .flatMap(replyChannel -> replyChannel.createMessage("▶️ | **Now Playing:** " + track.getInfo().title))
                    .subscribe();
        }
        else {
            event.getMessage().getChannel()
                    .flatMap(replyChannel -> replyChannel.createMessage("🎛 | Nothing is playing..."))
                    .subscribe();
        }
    }

}

