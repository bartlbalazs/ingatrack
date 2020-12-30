package hu.bartl.ingatrack.service;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.ViewDefinition;
import hu.bartl.ingatrack.config.BigQueryConfig;
import hu.bartl.ingatrack.config.BigQueryConfig.PropertySubscriptionView;
import hu.bartl.ingatrack.config.BigQueryConfig.SearchSubscriptionView;
import hu.bartl.ingatrack.config.BigQueryConfig.SubscriptionTable;
import hu.bartl.ingatrack.config.BigQueryConfig.TrackingTable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@AllArgsConstructor
@Slf4j
public class BqInitService {

    private final BigQueryConfig bigQueryConfig;
    private final BigQuery bigquery;

    @PostConstruct
    public void onStart() {
        createDataset(BigQueryConfig.LOCATION, bigQueryConfig.getDatasetName());

        createTable(bigQueryConfig.getDatasetName(), TrackingTable.NAME, TrackingTable.SCHEMA);
        createTable(bigQueryConfig.getDatasetName(), SubscriptionTable.NAME, SubscriptionTable.SCHEMA);

        createView(bigQueryConfig.getDatasetName(), PropertySubscriptionView.NAME, PropertySubscriptionView.QUERY);
        createView(bigQueryConfig.getDatasetName(), SearchSubscriptionView.NAME, SearchSubscriptionView.QUERY);
    }

    private void createDataset(String location, String datasetName) {
        var datasetId = DatasetId.of(datasetName);
        var dataset = bigquery.getDataset(datasetId);
        if (dataset == null) {
            bigquery.create(DatasetInfo.newBuilder(datasetId)
                    .setLocation(location)
                    .build());
        }
    }

    private void createTable(String datasetName, String name, Schema schema) {
        var trackingTableId = TableId.of(datasetName, name);
        var trackingTable = bigquery.getTable(trackingTableId);
        if (trackingTable == null) {
            bigquery.create(TableInfo.newBuilder(
                    trackingTableId,
                    StandardTableDefinition.of(schema))
                    .build());
        }
    }

    private void createView(String datasetName, String name, String query) {
        var propertySubscriptionTableId = TableId.of(datasetName, name);
        var propertySubscriptionTable = bigquery.getTable(propertySubscriptionTableId);
        if (propertySubscriptionTable == null) {
            var viewDefinition = ViewDefinition.newBuilder(query.replace("${dataset}", datasetName)).setUseLegacySql(false).build();
            bigquery.create(TableInfo.of(propertySubscriptionTableId, viewDefinition));
        }
    }
}
