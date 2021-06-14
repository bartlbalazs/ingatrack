package hu.bartl.ingatrack.service;

import com.google.api.client.util.Lists;
import hu.bartl.ingatrack.component.DateProvider;
import hu.bartl.ingatrack.component.HtmlPageParser;
import hu.bartl.ingatrack.config.ApplicationConfig;
import hu.bartl.ingatrack.entity.TrackingData;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jsoup.Connection.Method.GET;

@Service
@AllArgsConstructor
@Slf4j
public class TrackingDataService {

    private final ApplicationConfig applicationConfig;
    private final HtmlPageParser htmlPageParser;
    private final DateProvider dateProvider;

    @SneakyThrows
    @Retryable(value = org.jsoup.HttpStatusException.class, maxAttempts = 15, backoff = @Backoff(delay = 1000 * 180, multiplier = 2, maxDelay = 1000* 180 * 2))
    public TrackingData fetchTrackingData(long propertyId) {
        log.info("Fetching tracking data for property with ID: " + propertyId);
        TimeUnit.MILLISECONDS.sleep(applicationConfig.getRequestDelayMs());
        var htmlPage = Jsoup.connect(applicationConfig.getDatasourceUrl() + "/" + propertyId).method(GET).ignoreHttpErrors(true).execute();
        if (htmlPage.statusCode() == HttpStatus.OK.value()) {
            String html = new String(htmlPage.bodyAsBytes(), UTF_8.name());
            return htmlPageParser.parsePropertyPage(propertyId, html);
        } else {
            return TrackingData.create(propertyId, dateProvider.now());
        }
    }

    @SneakyThrows
    @Retryable(value = org.jsoup.HttpStatusException.class, maxAttempts = 15, backoff = @Backoff(delay = 1000 * 180, multiplier = 2, maxDelay = 1000* 180 * 2))
    public List<Long> fetchPropertyIds(String query) {
        log.info("Fetching property IDs from query: " + query);
        TimeUnit.MILLISECONDS.sleep(applicationConfig.getRequestDelayMs());
        List<Long> result = Lists.newArrayList();

        var htmlPage = Jsoup.connect(applicationConfig.getDatasourceUrl() + "/lista/" + query + "?page=1").method(GET).execute();
        var page = htmlPageParser.parseSearchPage(new String(htmlPage.bodyAsBytes(), UTF_8.name()));
        result.addAll(page.getProperties());

        while (page.hasNext()) {
            htmlPage = Jsoup.connect(applicationConfig.getDatasourceUrl() + "/lista/" + query + "?page=" + page.getNextPage()).method(GET).execute();
            page = htmlPageParser.parseSearchPage(new String(htmlPage.bodyAsBytes(), UTF_8.name()));
            result.addAll(page.getProperties());
        }

        return result;
    }
}
