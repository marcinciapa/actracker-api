package ovh.equino.actracker.notification.outbox.activity;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.activity.ActivityChangedNotification;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityNotifier;
import ovh.equino.actracker.notification.outbox.NotificationsOutboxRepository;

class OutboxActivityNotifier implements ActivityNotifier {

    private final NotificationsOutboxRepository outbox;

    OutboxActivityNotifier(NotificationsOutboxRepository outbox) {
        this.outbox = outbox;
    }

    @Override
    public void notifyChanged(ActivityChangedNotification activityChangedNotification) {
        Notification<ActivityChangedNotification> notification = new Notification<>(
                activityChangedNotification.id(),
                activityChangedNotification
        );
        outbox.save(notification);
    }
}
