package hu.bartl.ingatrack.entity.subscription;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.cloud.Timestamp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

import static hu.bartl.ingatrack.config.ObjectMapperConfig.TIMESTAMP_FORMAT;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public abstract class Subscription {

    private String id = UUID.randomUUID().toString();
    private Boolean active = true;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIMESTAMP_FORMAT)
    private Timestamp createdAt = Timestamp.now();
    @Setter(AccessLevel.NONE)
    private String type;

    public Boolean isActive() {
        return active;
    }
}
