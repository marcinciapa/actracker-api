package ovh.equino.actracker.repository.jpa.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.jpa.JpaIntegrationTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.jpa.TestUtil.nextUUID;

abstract class JpaNotificationRepositoryIntegrationTest extends JpaIntegrationTest {

    private JpaNotificationRepository repository;

    @BeforeEach
    void init() {
        repository = new JpaNotificationRepository(entityManager);
    }

    @Test
    void shouldSaveAndGetNotification() {
        UUID notificationId = nextUUID();
        Notification<?> notification = new Notification<>(notificationId, BigDecimal.ONE);
        AtomicLong initialVersion = new AtomicLong();
        AtomicLong finalVersion = new AtomicLong();
        inTransaction(() -> {
            repository.save(notification);
            Optional<Notification<?>> foundNotification = repository.get(notificationId);
            assertThat(foundNotification).isPresent();
            assertThat(foundNotification.get())
                    .usingRecursiveComparison()
                    .ignoringFields("version")
                    .isEqualTo(notification);
            initialVersion.set(foundNotification.get().version());
        });
        inTransaction(() -> {
            repository.save(notification);
            Optional<Notification<?>> foundNotification = repository.get(notificationId);
            assertThat(foundNotification).isPresent();
            assertThat(foundNotification.get())
                    .usingRecursiveComparison()
                    .ignoringFields("version")
                    .isEqualTo(notification);
            finalVersion.set(foundNotification.get().version());
        });
        assertThat(finalVersion).hasValueGreaterThan(initialVersion.get());
        inTransaction(() -> {
            Optional<Notification<?>> foundNotification = repository.get(notificationId);
            assertThat(foundNotification).isPresent();
            assertThat(foundNotification.get())
                    .usingRecursiveComparison()
                    .ignoringFields("version")
                    .isEqualTo(notification);
            assertThat(foundNotification.get().version()).isEqualTo(finalVersion.get());
        });
    }

    @Test
    void shouldNotFindNonExistingNotification() {
        inTransaction(() -> {
            Optional<Notification<?>> foundNotification = repository.get(randomUUID());
            assertThat(foundNotification).isNotPresent();
        });
    }

    @Test
    void shouldDeleteNotification() {
        UUID notificationId = nextUUID();
        Notification<?> notification = new Notification<>(notificationId, BigDecimal.ONE);

        inTransaction(() -> repository.save(notification));

        inTransaction(() -> {
            repository.delete(notificationId);
            Optional<Notification<?>> foundNotification = repository.get(notificationId);
            assertThat(foundNotification).isNotPresent();
        });

        inTransaction(() -> {
            Optional<Notification<?>> foundNotification = repository.get(notificationId);
            assertThat(foundNotification).isNotPresent();
        });
    }
}
