package ovh.equino.actracker.datasource.jpa.notification;

import ovh.equino.actracker.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaNotificationDataSourceIntegrationTest extends JpaNotificationDataSourceIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
