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
public class SearchSubscription extends Subscription {

    public static final String TYPE = "search";

    private String query;

    @Override
    public String getType() {
        return TYPE;
    }
}
