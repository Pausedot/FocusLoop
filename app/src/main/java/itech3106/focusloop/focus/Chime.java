package itech3106.focusloop.focus;

import android.content.Context;
import android.media.MediaPlayer;

import itech3106.focusloop.R;

/** Plays the short completion chime and releases the player when it is done. */
public final class Chime {

    private Chime() {
    }

    public static void play(Context context) {
        try {
            MediaPlayer player = MediaPlayer.create(context.getApplicationContext(), R.raw.chime);
            if (player == null) {
                return;
            }
            player.setOnCompletionListener(MediaPlayer::release);
            player.setOnErrorListener((mp, what, extra) -> {
                mp.release();
                return true;
            });
            player.start();
        } catch (RuntimeException ignored) {
            // A missing or busy audio device should never break the session.
        }
    }
}
