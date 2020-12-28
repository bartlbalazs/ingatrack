package hu.bartl.ingatrack.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
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
    private Integer builtAfter;
    private Integer builtBefore;
    private boolean panel;
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private Set<TrackingData> trackingData;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Property property = (Property) o;

        if (propertyId != property.propertyId) return false;
        return id.equals(property.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + propertyId;
        return result;
    }

    @Override
    public String toString() {
        return "Property{" +
                "id='" + id + '\'' +
                ", propertyId=" + propertyId +
                ", city='" + city + '\'' +
                ", panel=" + panel +
                '}';
    }
}
