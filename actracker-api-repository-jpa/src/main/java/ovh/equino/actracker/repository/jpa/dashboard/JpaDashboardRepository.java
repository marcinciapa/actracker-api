package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardDto;
import ovh.equino.actracker.domain.dashboard.DashboardRepository;
import ovh.equino.actracker.repository.jpa.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

class JpaDashboardRepository extends JpaRepository implements DashboardRepository {

    private final DashboardMapper mapper = new DashboardMapper();

    @Override
    public void add(DashboardDto dashboard) {
        DashboardEntity dashboardEntity = mapper.toEntity(dashboard);
        entityManager.persist(dashboardEntity);
    }

    @Override
    public void update(UUID dashboardId, DashboardDto dashboard) {
        DashboardEntity dashboardEntity = mapper.toEntity(dashboard);
        dashboardEntity.id = dashboardId.toString();
        entityManager.merge(dashboardEntity);
    }

    @Override
    public Optional<DashboardDto> findById(UUID dashboardId) {
        DashboardQueryBuilder queryBuilder = new DashboardQueryBuilder(entityManager);

        CriteriaQuery<DashboardEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.hasId(dashboardId),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<DashboardEntity> typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList().stream()
                .findFirst()
                .map(mapper::toDto);
    }

    @Override
    public List<DashboardDto> find(EntitySearchCriteria searchCriteria) {
        DashboardQueryBuilder queryBuilder = new DashboardQueryBuilder(entityManager);

        CriteriaQuery<DashboardEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.isAccessibleFor(searchCriteria.searcher()),
                                queryBuilder.isNotDeleted(),
                                queryBuilder.isInPage(searchCriteria.pageId()),
                                queryBuilder.isNotExcluded(searchCriteria.excludeFilter())
                        )
                )
                .orderBy(queryBuilder.ascending("id"));

        TypedQuery<DashboardEntity> typedQuery = entityManager
                .createQuery(query)
                .setMaxResults(searchCriteria.pageSize());

        return typedQuery.getResultList().stream()
                .map(mapper::toDto)
                .toList();
    }
}