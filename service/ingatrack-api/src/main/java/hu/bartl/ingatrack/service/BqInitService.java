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
            log.info("Creating dataset: " + datasetId);
            bigquery.create(DatasetInfo.newBuilder(datasetId)
                    .setLocation(location)
                    .build());
        } else {
            log.info("Dataset already exists: " + datasetId);
        }
    }

    private void createTable(String datasetName, String name, Schema schema) {
        var tableId = TableId.of(datasetName, name);
        var table = bigquery.getTable(tableId);
        if (table == null) {
            log.info("Creating table: " + tableId);
            bigquery.create(TableInfo.newBuilder(
                    tableId,
                    StandardTableDefinition.of(schema))
                    .build());
        } else {
            log.info("Table already exists: " + tableId);
        }
    }

    private void createView(String datasetName, String name, String query) {
        var viewId = TableId.of(datasetName, name);
        var view = bigquery.getTable(viewId);
        if (view == null) {
            log.info("Creating table: " + viewId);
            var viewDefinition = ViewDefinition.newBuilder(query.replace("${dataset}", datasetName)).setUseLegacySql(false).build();
            bigquery.create(TableInfo.of(viewId, viewDefinition));
        } else {
            log.info("View already exists: " + viewId);
        }
    }
}
