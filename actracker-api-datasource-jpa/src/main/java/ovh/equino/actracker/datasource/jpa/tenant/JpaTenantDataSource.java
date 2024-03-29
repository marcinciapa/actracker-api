package ovh.equino.actracker.datasource.jpa.tenant;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.jpa.JpaDAO;

import java.util.Optional;

class JpaTenantDataSource extends JpaDAO implements TenantDataSource {

    JpaTenantDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Optional<TenantDto> findByUsername(String username) {

        SelectTenantQuery selectTenant = new SelectTenantQuery(entityManager);
        return selectTenant
                .where(selectTenant.predicate().hasUsername(username))
                .execute()
                .map(TenantProjection::toTenant);
    }
}
