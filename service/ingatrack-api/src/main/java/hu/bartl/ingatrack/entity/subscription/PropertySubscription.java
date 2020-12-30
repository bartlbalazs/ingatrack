package hu.bartl.ingatrack.entity.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class PropertySubscription extends Subscription {

    public static final String TYPE = "property";

    private long propertyId;

    @Override
    public String getType() {
        return TYPE;
    }
}
