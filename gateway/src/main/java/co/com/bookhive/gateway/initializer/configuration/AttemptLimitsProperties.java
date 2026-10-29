package co.com.bookhive.gateway.initializer.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bookhive.gateway.attempts")
public record AttemptLimitsProperties(int maxPerAccount, int maxPerOrigin, Duration window, Duration blockFor) {
}
