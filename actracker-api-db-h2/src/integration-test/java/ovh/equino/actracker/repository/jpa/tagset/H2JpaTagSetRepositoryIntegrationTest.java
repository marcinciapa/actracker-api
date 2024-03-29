package ovh.equino.actracker.repository.jpa.tagset;

import ovh.equino.actracker.jpa.IntegrationTestH2DataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class H2JpaTagSetRepositoryIntegrationTest extends JpaTagSetRepositoryIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestH2DataBase.INSTANCE;
    }
}
