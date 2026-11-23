package itech3106.focusloop.focus;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import itech3106.focusloop.Extras;
import itech3106.focusloop.R;

/** Builds the ongoing timer notification and the completion notification. */
public final class FocusNotifier {

    public static final String ACTION_PAUSE = "itech3106.focusloop.action.PAUSE";
    public static final String ACTION_RESUME = "itech3106.focusloop.action.RESUME";
    public static final String ACTION_STOP = "itech3106.focusloop.action.STOP";

    private static final String CHANNEL_TIMER = "focus_timer";
    private static final String CHANNEL_SESSIONS = "focus_sessions";
    private static final int NOTIFICATION_RUNNING = 1001;
    private static final int NOTIFICATION_COMPLETE = 1002;
    private static final int REQUEST_PAUSE = 21;
    private static final int REQUEST_RESUME = 22;
    private static final int REQUEST_STOP = 23;
    private static final int REQUEST_OPEN = 24;
    private static final int REQUEST_BREAK = 25;
    private static final int REQUEST_ANOTHER = 26;

    private FocusNotifier() {
    }

    public static void ensureChannels(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel timer = new NotificationChannel(
                CHANNEL_TIMER,
                context.getString(R.string.notification_channel_timer),
                NotificationManager.IMPORTANCE_LOW);
        timer.setShowBadge(false);
        manager.createNotificationChannel(timer);

        NotificationChannel sessions = new NotificationChannel(
                CHANNEL_SESSIONS,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(sessions);
    }

    public static void showRunning(Context context, String taskTitle, long endAt,
                                   boolean paused, long remainingMs) {
        if (!hasPermission(context)) {
            return;
        }

        Intent open = new Intent(context, PomodoroActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(context, REQUEST_OPEN, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent toggle = PendingIntent.getBroadcast(context,
                paused ? REQUEST_RESUME : REQUEST_PAUSE,
                new Intent(paused ? ACTION_RESUME : ACTION_PAUSE)
                        .setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getBroadcast(context, REQUEST_STOP,
                new Intent(ACTION_STOP).setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String label = taskTitle == null || taskTitle.isEmpty()
                ? context.getString(R.string.focus_task_default) : taskTitle;
        String text = paused
                ? context.getString(R.string.notification_paused_format, label)
                : label;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TIMER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notification_running_title))
                .setContentText(text)
                .setContentIntent(content)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        if (paused) {
            builder.setWhen(System.currentTimeMillis() - remainingMs);
        } else {
            builder.setWhen(endAt)
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true);
        }

        builder.addAction(paused ? R.drawable.ic_play_arrow : R.drawable.ic_pause,
                context.getString(paused
                        ? R.string.notification_action_resume
                        : R.string.notification_action_pause),
                toggle);
        builder.addAction(R.drawable.ic_check,
                context.getString(R.string.notification_action_stop), stop);

        NotificationManagerCompat.from(context).notify(NOTIFICATION_RUNNING, builder.build());
    }

    public static void showComplete(Context context, String taskId, String taskTitle, int minutes) {
        if (!hasPermission(context)) {
            return;
        }

        Intent open = new Intent(context, PomodoroActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(context, REQUEST_OPEN, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent breakIntent = new Intent(context, BreakActivity.class);
        breakIntent.putExtra(Extras.TASK_ID, taskId);
        breakIntent.putExtra(Extras.TASK_TITLE, taskTitle);
        PendingIntent takeBreak = PendingIntent.getActivity(context, REQUEST_BREAK, breakIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent another = new Intent(context, FiveSecondActivity.class);
        another.putExtra(Extras.TASK_ID, taskId);
        another.putExtra(Extras.TASK_TITLE, taskTitle);
        PendingIntent startAnother = PendingIntent.getActivity(context, REQUEST_ANOTHER, another,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String label = taskTitle == null || taskTitle.isEmpty()
                ? context.getString(R.string.focus_task_default) : taskTitle;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_SESSIONS)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(context.getString(
                                R.string.notification_text_format, label, minutes))
                        .setContentIntent(content)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .addAction(R.drawable.ic_timer,
                                context.getString(R.string.session_take_break), takeBreak)
                        .addAction(R.drawable.ic_play_arrow,
                                context.getString(R.string.session_start_another), startAnother);

        NotificationManagerCompat.from(context).notify(NOTIFICATION_COMPLETE, builder.build());
    }

    public static void cancelRunning(Context context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_RUNNING);
    }

    public static boolean hasPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }
}
