package ovh.equino.actracker.datasource.jpa.activity;

import ovh.equino.actracker.jpa.IntegrationTestH2DataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class H2JpaActivityDataSourceIntegrationTest extends JpaActivityDataSourceIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestH2DataBase.INSTANCE;
    }
}
