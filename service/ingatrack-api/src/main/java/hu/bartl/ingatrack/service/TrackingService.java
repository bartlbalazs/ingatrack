package hu.bartl.ingatrack.service;

import com.google.api.client.util.Lists;
import hu.bartl.ingatrack.component.DateProvider;
import hu.bartl.ingatrack.component.HtmlPageParser;
import hu.bartl.ingatrack.config.ApplicationConfig;
import hu.bartl.ingatrack.entity.TrackingData;
import hu.bartl.ingatrack.repository.TrackingDataRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.jsoup.Connection.Method.GET;

@Service
@AllArgsConstructor
@Slf4j
public class TrackingService {

    private static final String ISO_8859_1 = "ISO-8859-1";

    private final TrackingDataRepository trackingDataRepository;
    private final ApplicationConfig applicationConfig;
    private final HtmlPageParser htmlPageParser;
    private final DateProvider dateProvider;


    @SneakyThrows
    public void trackProperty(long propertyId, String requestSource) {
        var trackingData = fetchTrackingData(propertyId);
        trackingData.setInsertedBy(requestSource);
        trackingDataRepository.save(trackingData);
    }

    public void trackSearch(String query) {
        var trackingData = fetchPropertyIds(query)
                .stream()
                .map(this::fetchTrackingData)
                .collect(Collectors.toList());
        trackingData.forEach(t -> t.setInsertedBy(query));
        trackingDataRepository.save(trackingData);
    }

    @SneakyThrows
    public TrackingData fetchTrackingData(long propertyId) {
        var htmlPage = Jsoup.connect(applicationConfig.getDatasourceUrl() + "/" + propertyId).method(GET).ignoreHttpErrors(true).execute();
        if (htmlPage.statusCode() == HttpStatus.OK.value()) {
            String html = new String(htmlPage.bodyAsBytes(), ISO_8859_1);
            return htmlPageParser.parsePropertyPage(propertyId, html);
        } else {
            return TrackingData.create(propertyId, dateProvider.now());
        }
    }

    @SneakyThrows
    public List<Long> fetchPropertyIds(String query) {
        List<Long> result = Lists.newArrayList();

        var htmlPage = Jsoup.connect(applicationConfig.getDatasourceUrl() + "/lista/" + query + "?page=1").method(GET).execute();
        var page = htmlPageParser.parseSearchPage(new String(htmlPage.bodyAsBytes(), ISO_8859_1));
        result.addAll(page.getProperties());

        while (page.hasNext()) {
            htmlPage = Jsoup.connect(applicationConfig.getDatasourceUrl() + "/lista/" + query + "?page=" + page.getNextPage()).method(GET).execute();
            page = htmlPageParser.parseSearchPage(new String(htmlPage.bodyAsBytes(), ISO_8859_1));
            result.addAll(page.getProperties());
        }

        return result;
    }
}
