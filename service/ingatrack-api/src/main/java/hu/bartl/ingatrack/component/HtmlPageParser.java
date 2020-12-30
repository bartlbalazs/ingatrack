package hu.bartl.ingatrack.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.bartl.ingatrack.entity.TrackingData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class HtmlPageParser {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private final ObjectMapper objectMapper;
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
        return Integer.parseInt(squareFootage.ownText().split(" ")[0]);
    }

    private Integer getBuiltAfter(Document propertyPage) {
        return getYearOfBuilt(propertyPage, 0);
    }

    private Integer getBuiltBefore(Document propertyPage) {
        return getYearOfBuilt(propertyPage, 1);
    }

    private Integer getYearOfBuilt(Document propertyPage, int order) {
        var select = propertyPage.select("td:contains(Építés éve)");
        if (select.isEmpty()) {
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
        var squareMeters = propertyPage.getElementsByClass("parameter parameter-price").first().child(1);
        float priceInMillions = Float.parseFloat(squareMeters.ownText().replace(",", ".").split(" ")[0]);
        return Math.round(priceInMillions * 1000000);
    }

    @SneakyThrows
    public PageResult parseSearchPage(String htmlPage) {
        var searchPage = Jsoup.parse(htmlPage);
        var dataLayerScript = searchPage.getElementsByTag("script").get(2).data().trim();
        var dataLayerJson = dataLayerScript.substring("dataLayer.push(".length(), dataLayerScript.length() - ");".length());
        var dataLayer = objectMapper.readValue(dataLayerJson, Map.class);
        return PageResult.builder()
                .properties(((List<Integer>) dataLayer.get("itemId")).stream().map(Long::valueOf).collect(Collectors.toList()))
                .hasNext(Integer.parseInt(dataLayer.get("numberOfItems").toString()) == DEFAULT_PAGE_SIZE)
                .nextPage(Integer.parseInt(dataLayer.get("page").toString()) + 1)
                .build();
    }

    @Data
    @Builder
    public static class PageResult {
        private List<Long> properties;
        private boolean hasNext;
        private Integer nextPage;

        public boolean hasNext() {
            return this.hasNext;
        }
    }
}
