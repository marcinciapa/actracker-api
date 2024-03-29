package ovh.equino.actracker.datasource.jpa.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity;
import ovh.equino.actracker.jpa.dashboard.DashboardEntity_;
import ovh.equino.actracker.jpa.dashboard.DashboardShareEntity;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;
import ovh.equino.actracker.jpa.dashboard.DashboardShareEntity_;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableSet;

final class SelectShareJoinDashboardQuery extends MultiResultJpaQuery<DashboardShareEntity, ShareJoinDashboardProjection> {

    private final Join<DashboardShareEntity, DashboardEntity> dashboard;
    private final PredicateBuilder predicate;

    SelectShareJoinDashboardQuery(EntityManager entityManager) {
        super(entityManager);
        this.dashboard = root.join(DashboardShareEntity_.dashboard);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        ShareJoinDashboardProjection.class,
                        root.get(DashboardShareEntity_.granteeId),
                        dashboard.get(DashboardEntity_.id),
                        root.get(DashboardShareEntity_.granteeName)
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

        public JpaPredicate hasDashboardId(UUID dashboardId) {
            return () -> criteriaBuilder.equal(dashboard.get(DashboardEntity_.id), dashboardId.toString());
        }

        public JpaPredicate hasDashboardIdIn(Collection<UUID> dashboardIds) {
            Set<String> dashboardIdsAsStrings = dashboardIds
                    .stream()
                    .map(UUID::toString)
                    .collect(toUnmodifiableSet());
            return in(dashboardIdsAsStrings, dashboard.get(DashboardEntity_.id));
        }

        public JpaPredicate isAccessibleFor(User searcher) {
            return () -> criteriaBuilder.equal(dashboard.get(DashboardEntity_.creatorId), searcher.id().toString());
        }
    }
}
