package backend.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DbConfiguration /*implements InitializingBean*/ {

	/*private final DataSource dataSource;
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
		final var query = getQuery("count_admins");
		try (
				final var connection = dataSource.getConnection();
				final var statement = connection.prepareStatement(query);
		) {
			final var rs = statement.executeQuery();
			Assert.isTrue(rs.next(), "Unable to count users!");

			final var count = rs.getLong(1);
			if (count < 1) {
				createDefaultAdminUser(connection);
			}
		}
	}

	private void createDefaultAdminUser(Connection connection) throws Exception {
		final var createUserQuery = getQuery("create_user");
		final var createRoleQuery = getQuery("create_role");
		final var assignRoleQuery = getQuery("assign_role");
		try (
				final var createUserStatement = connection.prepareStatement(createUserQuery);
				final var createRoleStatement = connection.prepareStatement(createRoleQuery);
				final var assignRoleStatement = connection.prepareStatement(assignRoleQuery);
		) {
			connection.setAutoCommit(false);

			final var userId = UUID.randomUUID().toString();
			final var passwordHash = encoder.encode("super secret");
			createUserStatement.setString(1, userId);
			createUserStatement.setString(2, "admin");
			createUserStatement.setString(3, passwordHash);
			createUserStatement.execute();

			final var adminRoleId = UUID.randomUUID().toString();
			createRoleStatement.setString(1, adminRoleId);
			createRoleStatement.setString(2, "ADMIN");
			createRoleStatement.execute();

			final var userRoleId = UUID.randomUUID().toString();
			createRoleStatement.setString(1, userRoleId);
			createRoleStatement.setString(2, "USER");
			createRoleStatement.execute();

			assignRoleStatement.setString(1, userId);
			assignRoleStatement.setString(2, adminRoleId);
			assignRoleStatement.execute();
			assignRoleStatement.setString(2, userRoleId);
			assignRoleStatement.execute();

			connection.commit();
		}
		catch (Exception e) {
			connection.rollback();
			throw e;
		}
	}*/

}
