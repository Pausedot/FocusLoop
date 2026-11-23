package itech3106.focusloop.reminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import itech3106.focusloop.R;

/**
 * Inexact daily reminders. AlarmManager is used with setAndAllowWhileIdle, so no exact
 * alarm permission is required. Each fire reschedules the next day.
 */
public final class ReminderScheduler {

    public static final String ACTION_TASKS = "itech3106.focusloop.reminder.TASKS";
    public static final String ACTION_REVIEW = "itech3106.focusloop.reminder.REVIEW";
    public static final String EXTRA_TYPE = "type";
    public static final String TYPE_TASKS = "tasks";
    public static final String TYPE_REVIEW = "review";

    public static final String CHANNEL = "focus_reminders";

    private static final int REQUEST_TASKS = 31;
    private static final int REQUEST_REVIEW = 32;

    private ReminderScheduler() {
    }

    public static void scheduleAll(Context context) {
        scheduleTasks(context);
        scheduleReview(context);
    }

    public static void scheduleTasks(Context context) {
        schedule(context, ACTION_TASKS, TYPE_TASKS, REQUEST_TASKS, 8, 0);
    }

    public static void scheduleReview(Context context) {
        schedule(context, ACTION_REVIEW, TYPE_REVIEW, REQUEST_REVIEW, 21, 0);
    }

    public static void ensureChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);
    }

    private static void schedule(Context context, String action, String type,
                                 int requestCode, int hour, int minute) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            return;
        }
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_TYPE, type);
        PendingIntent pending = PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next(hour, minute), pending);
    }

    private static long next(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return calendar.getTimeInMillis();
    }
}
