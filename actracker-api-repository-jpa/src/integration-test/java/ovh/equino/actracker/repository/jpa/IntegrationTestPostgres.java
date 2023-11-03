package ovh.equino.actracker.repository.jpa;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class IntegrationTestPostgres implements IntegrationTestRelationalDataBase {

    private final PostgreSQLContainer<?> container;

    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;

    public IntegrationTestPostgres() {
        container = new PostgreSQLContainer<>("postgres:15.1");
        container.start();
        this.jdbcUrl = container.getJdbcUrl();
        this.username = container.getUsername();
        this.password = container.getPassword();
        this.driverClassName = container.getDriverClassName();
        migrateSchema();
    }

    @Override
    public void addUser(TenantDto user) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        PreparedStatement preparedStatement = connection.prepareStatement(
                "insert into tenant (id, username, password) values (?, ?, ?);"
        );
        preparedStatement.setString(1, user.id().toString());
        preparedStatement.setString(2, user.username());
        preparedStatement.setString(3, user.password());
        preparedStatement.execute();
    }

    @Override
    public String jdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String username() {
        return username;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String driverClassName() {
        return driverClassName;
    }

    private void migrateSchema() {
        Flyway flyway = Flyway.configure()
                .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                .locations("classpath:schema/")
                .load();
        flyway.migrate();
    }
}