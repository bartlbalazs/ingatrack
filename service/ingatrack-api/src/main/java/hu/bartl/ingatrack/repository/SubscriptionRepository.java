package hu.bartl.ingatrack.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.google.cloud.Timestamp;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.QueryJobConfiguration;
import hu.bartl.ingatrack.config.BigQueryConfig;
import hu.bartl.ingatrack.entity.subscription.PropertySubscription;
import hu.bartl.ingatrack.entity.subscription.SearchSubscription;
import hu.bartl.ingatrack.entity.subscription.Subscription;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class SubscriptionRepository {

    private static final TypeReference MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private final BigQueryConfig bigQueryConfig;
    private final BigQuery bigquery;
    private final ObjectMapper objectMapper;

    public void save(Subscription subscription) {
        log.info("Storing subscription data to BigQuery: {}", subscription);

        Map<String, Object> subscriptionMap = convertToMap(subscription);
        var response = bigquery.insertAll(InsertAllRequest.of(
                bigQueryConfig.getDatasetName(),
                BigQueryConfig.SubscriptionTable.NAME,
                InsertAllRequest.RowToInsert.of(subscriptionMap))
        );

        if (!CollectionUtils.isEmpty(response.getInsertErrors())) {
            log.error("Failed to insert subscription data: {}. Error: {}", subscription, response.getInsertErrors());
        }
    }

    private Map<String, Object> convertToMap(Subscription subscription) {
        return (Map<String, Object>) objectMapper.convertValue(subscription, MAP_TYPE);
    }

    @SneakyThrows
    public Optional<PropertySubscription> findPropertySubscription(long propertyId) {
        var queryTemplate = "SELECT * EXCEPT(query, type) FROM `${dataset}.${table}` where ${propertyIdColumn} = ${propertyId}";
        var query = StringSubstitutor.replace(queryTemplate, Map.of(
                "dataset", bigQueryConfig.getDatasetName(),
                "table", BigQueryConfig.PropertySubscriptionView.NAME,
                "propertyIdColumn", PropertySubscription.Fields.propertyId,
                "propertyId", propertyId));

        var result = bigquery.query(QueryJobConfiguration.newBuilder(query).build());

        var resultIterator = result.getValues().iterator();
        return resultIterator.hasNext() ? Optional.of(convertToPropertySubscription(resultIterator.next())) : Optional.empty();
    }

    private PropertySubscription convertToPropertySubscription(FieldValueList subscription) {
        var propertySubscription = new PropertySubscription(subscription.get(PropertySubscription.Fields.propertyId).getLongValue());
        propertySubscription.setId(subscription.get(Subscription.Fields.id).getStringValue());
        propertySubscription.setActive(subscription.get(Subscription.Fields.active).getBooleanValue());
        propertySubscription.setCreatedAt(Timestamp.ofTimeMicroseconds(subscription.get(Subscription.Fields.createdAt).getTimestampValue()));
        return propertySubscription;
    }

    @SneakyThrows
    public Optional<SearchSubscription> findSearchSubscription(String query) {
        var queryTemplate = "SELECT * FROM `${dataset}.${table}` WHERE ${queryColumn} = '${query}'";
        var searchQuery = StringSubstitutor.replace(queryTemplate, Map.of(
                "dataset", bigQueryConfig.getDatasetName(),
                "table", BigQueryConfig.SearchSubscriptionView.NAME,
                "queryColumn", SearchSubscription.Fields.query,
                "query", query));

        var result = bigquery.query(QueryJobConfiguration.newBuilder(searchQuery).build());

        var resultIterator = result.getValues().iterator();
        return resultIterator.hasNext() ? Optional.of(convertToSearchSubscription(resultIterator.next())) : Optional.empty();
    }

    private SearchSubscription convertToSearchSubscription(FieldValueList subscription) {
        var searchSubscription = new SearchSubscription(subscription.get(SearchSubscription.Fields.query).getStringValue());
        searchSubscription.setId(subscription.get(Subscription.Fields.id).getStringValue());
        searchSubscription.setActive(subscription.get(Subscription.Fields.active).getBooleanValue());
        searchSubscription.setCreatedAt(Timestamp.ofTimeMicroseconds(subscription.get(Subscription.Fields.createdAt).getTimestampValue()));
        return searchSubscription;
    }

    @SneakyThrows
    public List<PropertySubscription> listActivePropertySubscriptions() {
        var queryTemplate = "SELECT * FROM `${dataset}.${table}` where ${activeColumn} = true";
        var query = StringSubstitutor.replace(queryTemplate, Map.of(
                "dataset", bigQueryConfig.getDatasetName(),
                "table", BigQueryConfig.PropertySubscriptionView.NAME,
                "activeColumn", Subscription.Fields.active
        ));

        var queryResult = bigquery.query(QueryJobConfiguration.newBuilder(query).build());

        List<PropertySubscription> result = Lists.newArrayList();
        queryResult.iterateAll().forEach(fieldValues -> result.add(convertToPropertySubscription(fieldValues)));
        return result;
    }

    @SneakyThrows
    public List<SearchSubscription> listActiveSearchSubscriptions() {
        var queryTemplate = "SELECT * FROM `${dataset}.${table}` where ${activeColumn} = true";
        var query = StringSubstitutor.replace(queryTemplate, Map.of(
                "dataset", bigQueryConfig.getDatasetName(),
                "table", BigQueryConfig.SearchSubscriptionView.NAME,
                "activeColumn", Subscription.Fields.active
        ));

        var queryResult = bigquery.query(QueryJobConfiguration.newBuilder(query).build());

        List<SearchSubscription> result = Lists.newArrayList();
        queryResult.iterateAll().forEach(fieldValues -> result.add(convertToSearchSubscription(fieldValues)));
        return result;
    }
}
