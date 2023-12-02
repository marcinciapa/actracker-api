package ovh.equino.actracker.repository.jpa.notification;

import ovh.equino.actracker.repository.jpa.IntegrationTestH2DataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;

class H2JpaNotificationDataSourceIntegrationTest extends JpaNotificationDataSourceIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestH2DataBase.INSTANCE;
    }
}