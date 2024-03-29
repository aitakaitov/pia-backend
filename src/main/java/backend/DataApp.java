package backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//@EnableSwagger2
@SpringBootApplication
@EnableAutoConfiguration
public class DataApp {
	public static void main(String[] args) {
		SpringApplication.run(DataApp.class, args);
	}
}
