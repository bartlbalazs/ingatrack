package hu.bartl.ingatrack.entity.subscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gcp.data.datastore.core.mapping.Entity;
import org.springframework.data.annotation.Id;

import java.util.UUID;

import static hu.bartl.ingatrack.config.ObjectMapperConfig.TIMESTAMP_FORMAT;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PropertySubscription {

    @Id
    @Default
    private String id = UUID.randomUUID().toString();
    private long propertyId;
    @Default
    private boolean active = true;
    @Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIMESTAMP_FORMAT)
    private Timestamp createdAt = Timestamp.now();
}
