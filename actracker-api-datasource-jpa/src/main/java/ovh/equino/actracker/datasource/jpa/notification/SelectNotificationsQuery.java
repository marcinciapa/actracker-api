package ovh.equino.actracker.datasource.jpa.notification;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.jpa.notification.NotificationEntity;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;
import ovh.equino.actracker.jpa.notification.NotificationEntity_;

final class SelectNotificationsQuery extends MultiResultJpaQuery<NotificationEntity, NotificationProjection> {

    SelectNotificationsQuery(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        NotificationProjection.class,
                        root.get(NotificationEntity_.id),
                        root.get(NotificationEntity_.version),
                        root.get(NotificationEntity_.dataType),
                        root.get(NotificationEntity_.data)
                )
        );
    }

    @Override
    protected Class<NotificationEntity> getRootEntityType() {
        return NotificationEntity.class;
    }

    @Override
    protected Class<NotificationProjection> getProjectionType() {
        return NotificationProjection.class;
    }

    /**
     * Deprecated: Filtering this entity is not supported. An attempt will throw RuntimeException.
     */
    public JpaPredicateBuilder<NotificationEntity> predicate() {
        throw new RuntimeException("Filtering notifications not supported");
    }

    /**
     * Deprecated: Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<NotificationEntity> sort() {
        throw new RuntimeException("Sorting notifications not supported");
    }
}
