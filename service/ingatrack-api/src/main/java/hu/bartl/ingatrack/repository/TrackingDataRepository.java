package hu.bartl.ingatrack.repository;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.Timestamp;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.QueryJobConfiguration;
import hu.bartl.ingatrack.config.BigQueryConfig;
import hu.bartl.ingatrack.entity.TrackingData;
import hu.bartl.ingatrack.entity.TrackingData.Fields;
import hu.bartl.ingatrack.entity.TrackingData.Property;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class TrackingDataRepository {

    private final BigQueryConfig bigQueryConfig;
    private final BigQuery bigquery;

    public void save(TrackingData trackingData) {
        log.info("Storing tracking data to BigQuery: {}", trackingData);

        var row = new TableRow();
        row.put(Fields.id, trackingData.getId());
        row.put(Fields.active, trackingData.isActive());
        row.put(Fields.createdAt, trackingData.getCreatedAt());
        row.put(Fields.price, trackingData.getPrice());
        row.put(Fields.property, trackingData.getProperty());

        var response = bigquery.insertAll(InsertAllRequest.of(bigQueryConfig.getDatasetName(), BigQueryConfig.TrackingTable.NAME,
                InsertAllRequest.RowToInsert.of(Map.of(
                        Fields.id, trackingData.getId(),
                        Fields.active, trackingData.isActive(),
                        Fields.createdAt, trackingData.getCreatedAt().toString(),

                        Fields.price, trackingData.getPrice(),
                        Fields.property, Map.of(
                                Property.Fields.propertyId, trackingData.getProperty().getPropertyId(),
                                Property.Fields.city, trackingData.getProperty().getCity(),
                                Property.Fields.squareMeters, trackingData.getProperty().getSquareMeters(),
                                Property.Fields.builtAfter, trackingData.getProperty().getBuiltAfter(),
                                Property.Fields.builtBefore, trackingData.getProperty().getBuiltBefore(),
                                Property.Fields.panel, trackingData.getProperty().isPanel()
                        )
                ))
        ));
        if (!CollectionUtils.isEmpty(response.getInsertErrors())) {
            log.error("Failed to insert tracking data: {}. Error: {}", trackingData, response.getInsertErrors());
        }
    }

    @SneakyThrows
    public Optional<TrackingData> findLatestByPropertyId(long propertyId) {
        var trackingDataRows = bigquery.query(QueryJobConfiguration.newBuilder(
                StringSubstitutor.replace("SELECT * FROM `${dataset}.${table}` where property.propertyId = ${propertyId} order by createdAt DESC LIMIT 1",
                        Map.of("dataset", bigQueryConfig.getDatasetName(), "table", BigQueryConfig.TrackingTable.NAME, "propertyId", propertyId)))
                .build());

        if (trackingDataRows.getValues().iterator().hasNext()) {
            var trackingDataRow = trackingDataRows.getValues().iterator().next();
            var propertyRow = trackingDataRow.get(Fields.property).getRecordValue();
            var trackingData = TrackingData
                    .builder()
                    .id(trackingDataRow.get(Fields.id).getStringValue())
                    .active(trackingDataRow.get(Fields.active).getBooleanValue())
                    .price(trackingDataRow.get(Fields.price).getLongValue())
                    .createdAt(Timestamp.ofTimeMicroseconds(trackingDataRow.get(Fields.createdAt).getTimestampValue()))
                    .property(Property.builder()
                            .propertyId(propertyRow.get(Property.Fields.propertyId).getLongValue())
                            .city(propertyRow.get(Property.Fields.city).getStringValue())
                            .builtAfter(Math.toIntExact(propertyRow.get(Property.Fields.builtAfter).getLongValue()))
                            .builtBefore(Math.toIntExact(propertyRow.get(Property.Fields.builtBefore).getLongValue()))
                            .squareMeters(Math.toIntExact(propertyRow.get(Property.Fields.squareMeters).getLongValue()))
                            .panel(propertyRow.get(Property.Fields.panel).getBooleanValue())
                            .build())
                    .build();
            return Optional.of(trackingData);
        } else {
            return Optional.empty();
        }
    }
}
