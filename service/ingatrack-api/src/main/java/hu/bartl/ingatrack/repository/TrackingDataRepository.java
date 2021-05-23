package hu.bartl.ingatrack.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.Timestamp;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.RowToInsert;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class TrackingDataRepository {

    private static final TypeReference MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private final BigQueryConfig bigQueryConfig;
    private final BigQuery bigquery;
    private final ObjectMapper objectMapper;

    public void save(TrackingData trackingData) {
        log.info("Storing tracking data to BigQuery: {}", trackingData);

        Map<String, Object> trackingDataMap = convertToMap(trackingData);
        var response = bigquery.insertAll(InsertAllRequest.of(
                bigQueryConfig.getDatasetName(),
                BigQueryConfig.TrackingTable.NAME,
                RowToInsert.of(trackingDataMap))
        );

        if (!CollectionUtils.isEmpty(response.getInsertErrors())) {
            log.error("Failed to insert tracking data: {}. Error: {}", trackingData, response.getInsertErrors());
        }
    }

    public void save(List<TrackingData> trackingData) {
        log.info("Storing tracking data to BigQuery: {}", trackingData);

        if (trackingData.isEmpty()) {
            log.warn("Tracking data is empty.");
            return;
        }

        var rowsToInsert = trackingData.stream()
                .map(this::convertToMap)
                .map(RowToInsert::of)
                .collect(Collectors.toList());

        var response = bigquery.insertAll(InsertAllRequest.of(
                bigQueryConfig.getDatasetName(),
                BigQueryConfig.TrackingTable.NAME,
                rowsToInsert)
        );

        if (!CollectionUtils.isEmpty(response.getInsertErrors())) {
            log.error("Failed to insert tracking data: {}. Error: {}", trackingData, response.getInsertErrors());
        }
    }

    private Map<String, Object> convertToMap(TrackingData trackingData) {
        return (Map<String, Object>) objectMapper.convertValue(trackingData, MAP_TYPE);
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
                    .listingType(getNullableString(trackingDataRow, Fields.listingType))
                    .createdAt(Timestamp.ofTimeMicroseconds(trackingDataRow.get(Fields.createdAt).getTimestampValue()))
                    .property(Property.builder()
                            .propertyId(propertyRow.get(Property.Fields.propertyId).getLongValue())
                            .propertyType(getNullableString(propertyRow, Property.Fields.propertyType))
                            .propertySubType(getNullableString(propertyRow, Property.Fields.propertySubType))
                            .county(getNullableString(propertyRow, Property.Fields.county))
                            .city(getNullableString(propertyRow, Property.Fields.city))
                            .zone(getNullableString(propertyRow, Property.Fields.zone))
                            .street(getNullableString(propertyRow, Property.Fields.street))
                            .builtAfter(getNullableInt(propertyRow, Property.Fields.builtAfter))
                            .conditionType(getNullableString(propertyRow,Property.Fields.conditionType))
                            .builtBefore(getNullableInt(propertyRow, Property.Fields.builtBefore))
                            .squareMeters(getNullableInt(propertyRow, Property.Fields.squareMeters))
                            .build())
                    .build();
            return Optional.of(trackingData);
        } else {
            return Optional.empty();
        }
    }

    private String getNullableString(FieldValueList propertyRow, String fieldName) {
        return !propertyRow.get(fieldName).isNull() ? propertyRow.get(fieldName).getStringValue() : null;
    }

    private Integer getNullableInt(FieldValueList propertyRow, String fieldName) {
        return !propertyRow.get(fieldName).isNull() ? Math.toIntExact(propertyRow.get(fieldName).getLongValue()) : null;
    }
}
