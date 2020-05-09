package hu.bartl.ingatrack.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Property {

    @Id
    @Default
    private String id = UUID.randomUUID().toString();
    private int propertyId;
    private String city;
    private int squareFootage;
    private int builtAfter;
    private int builtBefore;
    private boolean panel;
    @OneToMany
    private Set<TrackingData> trackingData;
}
