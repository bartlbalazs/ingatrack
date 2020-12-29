package hu.bartl.ingatrack.config;

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import hu.bartl.ingatrack.entity.TrackingData;
import hu.bartl.ingatrack.entity.TrackingData.Property;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.cloud.gcp.bigquery")
public class BigQueryConfig {

    public static final String LOCATION = "EU";

    private String datasetName;

    public static class TrackingTable {
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
}
