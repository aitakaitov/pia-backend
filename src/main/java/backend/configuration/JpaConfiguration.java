package backend.configuration;

import backend.model.repo.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackageClasses = {
		UserRepository.class
})
@EnableTransactionManagement
public class JpaConfiguration {

}
