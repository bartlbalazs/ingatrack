package hu.bartl.ingatrack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "application.tracking")
public class TrackingServiceConfig {
}
