package hu.bartl.ingatrack.config;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import hu.bartl.ingatrack.entity.TrackingData;
import hu.bartl.ingatrack.entity.TrackingData.Property;
import hu.bartl.ingatrack.entity.subscription.PropertySubscription;
import hu.bartl.ingatrack.entity.subscription.SearchSubscription;
import hu.bartl.ingatrack.entity.subscription.Subscription;
import lombok.Data;
import lombok.experimental.UtilityClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.cloud.gcp.bigquery")
public class BigQueryConfig {

    public static final String LOCATION = "EU";

    private String datasetName;

    @UtilityClass
    public class TrackingTable {
        public static final String NAME = "tracking";
        public static final Schema SCHEMA = Schema.of(
                Field.of(TrackingData.Fields.id, StandardSQLTypeName.STRING),
                Field.of(TrackingData.Fields.property, StandardSQLTypeName.STRUCT,
                        Field.of(Property.Fields.propertyId, StandardSQLTypeName.INT64),
                        Field.of(Property.Fields.city, StandardSQLTypeName.STRING),
                        Field.of(Property.Fields.squareMeters, StandardSQLTypeName.INT64),
                        Field.of(Property.Fields.builtAfter, StandardSQLTypeName.INT64),
                        Field.of(Property.Fields.builtBefore, StandardSQLTypeName.INT64),
                        Field.of(Property.Fields.panel, StandardSQLTypeName.BOOL)
                ),
                Field.of(TrackingData.Fields.price, StandardSQLTypeName.INT64),
                Field.of(TrackingData.Fields.active, StandardSQLTypeName.BOOL),
                Field.of(TrackingData.Fields.createdAt, StandardSQLTypeName.TIMESTAMP),
                Field.of(TrackingData.Fields.insertedBy, StandardSQLTypeName.STRING)
        );
    }

    @UtilityClass
    public static class SubscriptionTable {
        public final String NAME = "subscription";
        public final Schema SCHEMA = Schema.of(
                Field.of(Subscription.Fields.id, StandardSQLTypeName.STRING),
                Field.of(PropertySubscription.Fields.propertyId, StandardSQLTypeName.INT64),
                Field.of(SearchSubscription.Fields.query, StandardSQLTypeName.STRING),
                Field.of(Subscription.Fields.active, StandardSQLTypeName.BOOL),
                Field.of(Subscription.Fields.createdAt, StandardSQLTypeName.TIMESTAMP),
                Field.of(Subscription.Fields.type, StandardSQLTypeName.STRING)
        );
    }

    @UtilityClass
    public static class PropertySubscriptionView {
        public final String NAME = "property-subscription";
        public final String QUERY = "SELECT array_agg(s ORDER BY createdAt DESC)[offset(0)].* except (query, type) FROM `${dataset}.subscription` s where type = \"property\" GROUP BY s.propertyId";
    }

    @UtilityClass
    public static class SearchSubscriptionView {
        public final String NAME = "search-subscription";
        public final String QUERY = "SELECT array_agg(s ORDER BY createdAt DESC)[offset(0)].* except (propertyId, type) FROM `${dataset}.subscription` s where type = \"search\" GROUP BY s.query";
    }
}
