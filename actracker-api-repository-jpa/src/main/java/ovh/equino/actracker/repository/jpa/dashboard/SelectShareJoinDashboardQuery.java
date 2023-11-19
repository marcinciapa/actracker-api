package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.JpaSortBuilder;
import ovh.equino.actracker.repository.jpa.MultiResultJpaQuery;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableSet;

final class SelectShareJoinDashboardQuery extends MultiResultJpaQuery<DashboardShareEntity, ShareJoinDashboardProjection> {

    private final Join<DashboardShareEntity, DashboardEntity> dashboard;
    private final PredicateBuilder predicate;

    SelectShareJoinDashboardQuery(EntityManager entityManager) {
        super(entityManager);
        this.dashboard = root.join("dashboard");
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
            criteriaBuilder.construct(
                    ShareJoinDashboardProjection.class,
                    root.get("granteeId"),
                    dashboard.get("id"),
                    root.get("granteeName")
            )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectShareJoinDashboardQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<DashboardShareEntity> getRootEntityType() {
        return DashboardShareEntity.class;
    }

    @Override
    protected Class<ShareJoinDashboardProjection> getProjectionType() {
        return ShareJoinDashboardProjection.class;
    }

    /**
     * @deprecated Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<DashboardShareEntity> sort() {
        throw new RuntimeException("Sorting shares joint with dashboards not supported");
    }


    public final class PredicateBuilder extends JpaPredicateBuilder<DashboardShareEntity> {
        PredicateBuilder() {
            super(criteriaBuilder, root);
        }


        @Override
        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(dashboard.get("deleted"));
        }

        public JpaPredicate hasDashboardId(UUID dashboardId) {
            return () -> criteriaBuilder.equal(dashboard.get("id"), dashboardId.toString());
        }

        public JpaPredicate hasTagIdIn(Collection<UUID> dashboardIds) {
            Set<String> tagIdsAsStrings = dashboardIds
                    .stream()
                    .map(UUID::toString)
                    .collect(toUnmodifiableSet());
            return in(tagIdsAsStrings, dashboard.get("id"));
        }

        @Override
        public JpaPredicate isAccessibleFor(User searcher) {
            return () -> criteriaBuilder.equal(dashboard.get("creatorId"), searcher.id().toString());
        }
    }
}