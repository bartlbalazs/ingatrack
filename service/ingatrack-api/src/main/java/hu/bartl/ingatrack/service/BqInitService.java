package hu.bartl.ingatrack.service;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import hu.bartl.ingatrack.config.BigQueryConfig;
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
        var datasetId = DatasetId.of(bigQueryConfig.getDatasetName());
        var dataset = bigquery.getDataset(datasetId);
        if (dataset == null) {
            bigquery.create(DatasetInfo.newBuilder(datasetId)
                    .setLocation(bigQueryConfig.LOCATION)
                    .build());
        }

        var tableId = TableId.of(bigQueryConfig.getDatasetName(), BigQueryConfig.TrackingTable.NAME);
        var table = bigquery.getTable(tableId);
        if (table == null) {
            bigquery.create(TableInfo.newBuilder(
                    tableId,
                    StandardTableDefinition.of(BigQueryConfig.TrackingTable.SCHEMA))
                    .build());
        }
    }
}
