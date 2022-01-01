package backend.configuration;

import backend.constants.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.FileCopyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DbConfiguration implements InitializingBean {

    private final DataSource dataSource;
	private final ResourceLoader resourceLoader;

	private final PasswordEncoder encoder;


	private String getQuery(final String name) throws IOException {
		final var resource = resourceLoader.getResource("classpath:/db/queries/" + name + ".sql");
		try (final var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
			return FileCopyUtils.copyToString(reader);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        try (final var connection = dataSource.getConnection()) {
            if (!hasRoles(connection)) {
                createRoles(connection);
            }

            if (!hasAdmin(connection)) {
                createAdmin(connection);
            }

            if (!hasPostTypes(connection)) {
                createPostTypes(connection);
            }
        }
	}

    private boolean hasAdmin(Connection connection) throws Exception {
        long count = getCount(connection, "count_admins");
        return count >= 1;
    }

    private boolean hasRoles(Connection connection) throws Exception {
        long count = getCount(connection, "count_roles");
        return count >= 2;

    }

    private boolean hasPostTypes(Connection connection) throws Exception {
        long count = getCount(connection, "count_post_types");
        return count >= 2;
    }

    private long getCount(Connection connection, String queryName) throws Exception {
        final var query = getQuery(queryName);
        final var statement = connection.prepareStatement(query);
        final var rs = statement.executeQuery();

        Assert.isTrue(rs.next(), "Unable to execute query " + queryName + "!");

        return rs.getLong(1);
    }

    private void createRoles(Connection connection) throws Exception {
        final var createRoleQuery1 = getQuery("create_role");
        final var statement1 = connection.prepareStatement(createRoleQuery1);
        statement1.setString(1, Constants.ADMIN_ROLE_NAME);
        statement1.execute();

        final var createRoleQuery2 = getQuery("create_role");
        final var statement2 = connection.prepareStatement(createRoleQuery2);
        statement2.setString(1, Constants.USER_ROLE_NAME);
        statement2.execute();
    }

    private void createPostTypes(Connection connection) throws Exception {
        final var createTypeRegular = getQuery("create_type");
        final var statement1 = connection.prepareStatement(createTypeRegular);
        statement1.setString(1, Constants.REGULAR_TYPE_NAME);
        statement1.execute();

        final var createTypeAnnouncement = getQuery("create_type");
        final var statement2 = connection.prepareStatement(createTypeAnnouncement);
        statement2.setString(1, Constants.ANNOUNCEMENT_TYPE_NAME);
        statement2.execute();
    }

    private void createAdmin(Connection connection) throws Exception {
        final var createUserQuery = getQuery("create_user");
        final var createUserStatement = connection.prepareStatement(createUserQuery);
        final var email = "admin@admin.admin";
        final var passwordPlain = generateAdminPassword();
        final var passwordHash = encoder.encode(passwordPlain);

        log.info("ADMIN EMAIL: " + email);
        log.info("ADMIN PASSWORD: " + passwordPlain);

        createUserStatement.setString(1, email);
        createUserStatement.setString(2, passwordHash);
        createUserStatement.setString(3, "admin");
        createUserStatement.setBoolean(4, false);
        createUserStatement.execute();

        final var assignRoleQuery = getQuery("assign_role");
        final var assignRoleStatement = connection.prepareStatement(assignRoleQuery);
        assignRoleStatement.setString(1, email);
        assignRoleStatement.setString(2, Constants.ADMIN_ROLE_NAME);
        assignRoleStatement.execute();
    }

    private String generateAdminPassword() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 20;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
