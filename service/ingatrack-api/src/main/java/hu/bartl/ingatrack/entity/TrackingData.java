package hu.bartl.ingatrack.entity;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

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
        private Integer squareMeters;
        private Integer builtAfter;
        private Integer builtBefore;
        @Default
        private Boolean panel = null;
    }
}
