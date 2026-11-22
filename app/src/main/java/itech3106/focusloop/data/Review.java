package itech3106.focusloop.data;

/** One saved daily review. */
public class Review {

    public final String id;
    public final int epochDay;
    public final String wentWell;
    public final String obstacle;
    public final String tomorrow;
    /** 1 (hard) to 3 (good); 0 when the user has not chosen. */
    public final int mood;
    /** App-private voice note path, or null. */
    public final String voicePath;
    public final long savedAt;

    public Review(String id, int epochDay, String wentWell, String obstacle, String tomorrow,
                  int mood, String voicePath, long savedAt) {
        this.id = id;
        this.epochDay = epochDay;
        this.wentWell = wentWell;
        this.obstacle = obstacle;
        this.tomorrow = tomorrow;
        this.mood = mood;
        this.voicePath = voicePath;
        this.savedAt = savedAt;
    }
}
