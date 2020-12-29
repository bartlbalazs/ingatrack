package hu.bartl.ingatrack.component;

import hu.bartl.ingatrack.entity.TrackingData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class HtmlPageParser {

    private final DateProvider dateProvider;

    public TrackingData parsePropertyPage(long propertyId, String propertyPageHtml) {
        var trackingData = TrackingData.create(propertyId, dateProvider.now());
        var propertyPage = Jsoup.parse(propertyPageHtml);

        var property = trackingData.getProperty();
        property.setCity(getCity(propertyPage));
        property.setSquareMeters(getSquareMeters(propertyPage));
        property.setBuiltAfter(getBuiltAfter(propertyPage));
        property.setBuiltBefore(getBuiltBefore(propertyPage));
        property.setPanel(isPanel(propertyPage));

        trackingData.setPrice(getPrice(propertyPage));
        trackingData.setActive(true);
        return trackingData;
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
