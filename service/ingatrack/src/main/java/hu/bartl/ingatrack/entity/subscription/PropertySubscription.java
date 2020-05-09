package hu.bartl.ingatrack.entity.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PropertySubscription {

    @Id
    @Default
    private String id = UUID.randomUUID().toString();
    @Column(unique = true)
    private int propertyId;
    @Default
    private boolean active = true;
    @Default
    private Instant createdAt = Instant.now();
}
