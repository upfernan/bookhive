package co.com.bookhive.initializer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "co.com.bookhive")
@EnableJpaRepositories(basePackages = "co.com.bookhive")
@EntityScan(basePackages = "co.com.bookhive")
public class BookhiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookhiveApplication.class, args);
	}

}
