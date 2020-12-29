package hu.bartl.ingatrack.entity;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class TrackingData {

    @Default
    private String id = UUID.randomUUID().toString();
    @Default
    private Property property = Property.builder().build();
    private long price;
    @Default
    private boolean active = false;
    private Timestamp createdAt;
    private String insertedBy;

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

    public static TrackingData create(long propertyId, Timestamp createdAt) {
        return TrackingData.builder()
                .property(Property.builder().propertyId(propertyId).build())
                .createdAt(createdAt)
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldNameConstants
    public static class Property {

        private long propertyId;
        private String city;
        private int squareMeters;
        private Integer builtAfter;
        private Integer builtBefore;
        private boolean panel;
    }
}
