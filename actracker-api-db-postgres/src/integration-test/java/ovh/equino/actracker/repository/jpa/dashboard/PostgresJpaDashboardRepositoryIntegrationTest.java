package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaDashboardRepositoryIntegrationTest extends JpaDashboardRepositoryIntegrationTest {


    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
