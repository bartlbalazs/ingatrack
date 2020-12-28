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
import java.util.Objects;
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
    private int price;
    private boolean active;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackingData that = (TrackingData) o;

        if (price != that.price) return false;
        if (active != that.active) return false;
        if (!id.equals(that.id)) return false;
        return Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + price;
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TrackingData{" +
                "id='" + id + '\'' +
                ", property=" + property != null ? String.valueOf(property.getPropertyId()) : null +
                ", createdAt=" + createdAt +
                ", price=" + price +
                ", active=" + active +
                '}';
    }
}
