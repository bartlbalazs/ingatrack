package hu.bartl.ingatrack.service;

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

import static org.jsoup.Connection.Method.GET;

@Service
@AllArgsConstructor
@Slf4j
public class TrackingService {

    private final TrackingDataRepository trackingDataRepository;
    private final ApplicationConfig applicationConfig;
    private final HtmlPageParser htmlPageParser;
    private final DateProvider dateProvider;


    @SneakyThrows
    public void trackProperty(long propertyId, String requestSource) {
        var htmlPage = Jsoup.connect(applicationConfig.getDatasourceUrl() + "/" + propertyId).method(GET).ignoreHttpErrors(true).execute();
        if (htmlPage.statusCode() == HttpStatus.OK.value()) {
            String html = new String(htmlPage.bodyAsBytes(), "ISO-8859-1");
            var trackingData = htmlPageParser.parsePropertyPage(propertyId, html);
            trackingData.setInsertedBy(requestSource);
            trackingDataRepository.save(trackingData);
        } else {
            trackingDataRepository.save(TrackingData.createWithRequestSource(propertyId, dateProvider.now(), requestSource));
        }
    }
}
