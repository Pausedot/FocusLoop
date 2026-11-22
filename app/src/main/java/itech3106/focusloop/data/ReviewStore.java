package itech3106.focusloop.data;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/** Saved daily reviews, one per local day. */
public class ReviewStore extends JsonStore<Review> {

    public ReviewStore(Context context) {
        super(context, "reviews_v1");
        seedOnce(Seed.reviews());
    }

    @Override
    protected String idOf(Review item) {
        return item.id;
    }

    @Override
    protected JSONObject toJson(Review review) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", review.id);
        json.put("epochDay", review.epochDay);
        json.put("wentWell", review.wentWell);
        json.put("obstacle", review.obstacle);
        json.put("tomorrow", review.tomorrow);
        json.put("mood", review.mood);
        json.put("voicePath", review.voicePath == null ? "" : review.voicePath);
        json.put("savedAt", review.savedAt);
        return json;
    }

    @Override
    protected Review fromJson(JSONObject json) throws JSONException {
        String voice = json.optString("voicePath", "");
        return new Review(
                json.getString("id"),
                json.optInt("epochDay", Dates.today()),
                json.optString("wentWell", ""),
                json.optString("obstacle", ""),
                json.optString("tomorrow", ""),
                json.optInt("mood", 0),
                voice.isEmpty() ? null : voice,
                json.optLong("savedAt", 0L));
    }

    public List<Review> recent() {
        List<Review> reviews = all();
        Collections.sort(reviews, (left, right) -> Integer.compare(right.epochDay, left.epochDay));
        return reviews;
    }

    public Review forDay(int epochDay) {
        for (Review review : all()) {
            if (review.epochDay == epochDay) {
                return review;
            }
        }
        return null;
    }

    /** One review per day: saving again replaces the previous answer. */
    public synchronized void save(Review review) {
        List<Review> reviews = all();
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).epochDay == review.epochDay) {
                reviews.set(i, review);
                replaceAll(reviews);
                return;
            }
        }
        reviews.add(review);
        replaceAll(reviews);
    }
}
