package hu.bartl.ingatrack.service;

import hu.bartl.ingatrack.component.DateProvider;
import hu.bartl.ingatrack.config.ApplicationConfig;
import hu.bartl.ingatrack.entity.TrackingData;
import hu.bartl.ingatrack.repository.TrackingDataRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import static org.jsoup.Connection.Method.GET;

@Service
@AllArgsConstructor
@Slf4j
public class TrackingService {

    private final TrackingDataRepository trackingDataRepository;
    private final ApplicationConfig applicationConfig;
    private final DateProvider dateProvider;


    @SneakyThrows
    public void trackProperty(long propertyId) {
        var trackingData = TrackingData.create(propertyId, dateProvider.now());
        try {
            var connection = Jsoup.connect(applicationConfig.getDatasourceUrl() + "/" + propertyId).method(GET);
            String html = new String(connection.execute().bodyAsBytes(), "ISO-8859-1");
            var propertyPage = Jsoup.parse(html);

            var property = trackingData.getProperty();
            property.setCity(getCity(propertyPage));
            property.setSquareMeters(getSquareMeters(propertyPage));
            property.setBuiltAfter(getBuiltAfter(propertyPage));
            property.setBuiltBefore(getBuiltBefore(propertyPage));
            property.setPanel(isPanel(propertyPage));

            trackingData.setPrice(getPrice(propertyPage));
            trackingData.setActive(true);
        } catch (Exception e) {
            log.warn("Failed to parse tracking data for property {}. Reson: {}", propertyId, e);
        }
        trackingDataRepository.save(trackingData);
    }

    private String getCity(Document propertyPage) {
        var title = propertyPage.getElementsByClass("js-listing-title").first();
        return title.ownText().split(",")[0];
    }

    private int getSquareMeters(Document propertyPage) {
        var squareFootage = propertyPage.getElementsByClass("parameter parameter-area-size").first().child(1);
        return Integer.valueOf(squareFootage.ownText().split(" ")[0]);
    }

    private Integer getBuiltAfter(Document propertyPage) {
        return getYearOfBuilt(propertyPage, 0);
    }

    private Integer getBuiltBefore(Document propertyPage) {
        return getYearOfBuilt(propertyPage, 1);
    }

    private Integer getYearOfBuilt(Document propertyPage, int order) {
        var select = propertyPage.select("td:contains(Építés éve)");
        if (select.size() == 0) {
            return null;
        }
        var yearsOfBuilt = select.first().parent().child(1).text();
        yearsOfBuilt = yearsOfBuilt.contains(" ") ? yearsOfBuilt.split(" ")[0] : yearsOfBuilt;
        return yearsOfBuilt.contains("-") ? Integer.valueOf(yearsOfBuilt.split("-")[order]) : Integer.valueOf(yearsOfBuilt);
    }

    private boolean isPanel(Document propertyPage) {
        return propertyPage.select("h2.card-title").first().text().contains("panel");
    }

    private int getPrice(Document propertyPage) {
        var squareFootage = propertyPage.getElementsByClass("parameter parameter-price").first().child(1);
        float priceInMillions = Float.valueOf(squareFootage.ownText().replace(",",".").split(" ")[0]);
        return Math.round(priceInMillions * 1000000);
    }
}
