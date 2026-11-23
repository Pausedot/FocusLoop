package itech3106.focusloop.reminder;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import itech3106.focusloop.R;
import itech3106.focusloop.data.Dates;
import itech3106.focusloop.data.ReviewStore;
import itech3106.focusloop.data.TaskStore;
import itech3106.focusloop.focus.FocusNotifier;
import itech3106.focusloop.planning.DailyReviewActivity;
import itech3106.focusloop.tasks.TaskInboxActivity;

/** Posts the due-task and evening-review reminders, then schedules the next day. */
public class ReminderReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_TASKS = 2001;
    private static final int NOTIFICATION_REVIEW = 2002;

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra(ReminderScheduler.EXTRA_TYPE);
        if (ReminderScheduler.TYPE_TASKS.equals(type)) {
            postTasks(context);
            ReminderScheduler.scheduleTasks(context);
        } else if (ReminderScheduler.TYPE_REVIEW.equals(type)) {
            postReview(context);
            ReminderScheduler.scheduleReview(context);
        }
    }

    private void postTasks(Context context) {
        if (!FocusNotifier.hasPermission(context)) {
            return;
        }
        int count = new TaskStore(context).dueOn(Dates.today()).size();
        if (count == 0) {
            return;
        }
        ReminderScheduler.ensureChannel(context);

        Intent open = new Intent(context, TaskInboxActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, 41, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, ReminderScheduler.CHANNEL)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(context.getString(R.string.reminder_tasks_title))
                        .setContentText(context.getString(
                                R.string.reminder_tasks_text_format, count))
                        .setContentIntent(pending)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat.from(context).notify(NOTIFICATION_TASKS, builder.build());
    }

    private void postReview(Context context) {
        if (!FocusNotifier.hasPermission(context)) {
            return;
        }
        if (new ReviewStore(context).forDay(Dates.today()) != null) {
            return;
        }
        ReminderScheduler.ensureChannel(context);

        Intent open = new Intent(context, DailyReviewActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, 42, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, ReminderScheduler.CHANNEL)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(context.getString(R.string.reminder_review_title))
                        .setContentText(context.getString(R.string.reminder_review_text))
                        .setContentIntent(pending)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat.from(context).notify(NOTIFICATION_REVIEW, builder.build());
    }
}
