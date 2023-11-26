package ovh.equino.actracker.repository.jpa.outbox;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.notification.outbox.NotificationDataSource;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;

class JpaNotificationDataSource extends JpaDAO implements NotificationDataSource {

    JpaNotificationDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<Notification<?>> getPage(int limit) {
        throw new RuntimeException("not implemented yet");
    }
}
