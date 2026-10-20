package co.com.bookhive.gateway.initializer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "co.com.bookhive.gateway")
public class GatewayApplication {

	public static void main(final String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
