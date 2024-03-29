package ovh.equino.actracker.jpa;

import ovh.equino.actracker.domain.tenant.TenantDto;

import static ovh.equino.actracker.jpa.TestUtil.nextUUID;
import static ovh.equino.actracker.jpa.TestUtil.randomString;

public final class TenantBuilder {

    private TenantDto newTenant;

    TenantBuilder() {
        newTenant = new TenantDto(nextUUID(), randomString(), randomString());
    }

    public TenantBuilder named(String name) {
        newTenant = new TenantDto(
                newTenant.id(),
                name,
                newTenant.password()
        );
        return this;
    }

    public TenantDto build() {
        return newTenant;
    }
}
