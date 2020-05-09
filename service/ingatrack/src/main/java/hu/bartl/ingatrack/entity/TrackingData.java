package hu.bartl.ingatrack.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class TrackingData {

    @Id
    @Default
    private String id = UUID.randomUUID().toString();
    @ManyToOne
    private Property property;
    private Instant createdAt;
    private boolean active;
}
