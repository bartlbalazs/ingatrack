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
        var propertyPage = Jsoup.parse(propertyPageHtml);
        var dataLayer = getDataLayer(propertyPage);
        var trackingData = TrackingData.create(propertyId, dateProvider.now());
        trackingData.setListingType(String.valueOf(dataLayer.get("listingType")));

        var property = trackingData.getProperty();
        property.setPropertyType(String.valueOf(dataLayer.get("propertyType")));
        property.setPropertySubType(String.valueOf(dataLayer.get("propertySubType")));
        property.setCounty(String.valueOf(dataLayer.get("county")));
        property.setCity(String.valueOf(dataLayer.get("city")));
        property.setZone(String.valueOf(dataLayer.get("zone")));
        property.setStreet(String.valueOf(dataLayer.get("street")));
        property.setConditionType(String.valueOf(dataLayer.get("conditionType")));
        property.setSquareMeters(Integer.parseInt(String.valueOf(dataLayer.get("area"))));
        property.setBuiltAfter(getBuiltAfter(propertyPage));
        property.setBuiltBefore(getBuiltBefore(propertyPage));

        trackingData.setPrice(trackingData.getListingType().equals("Eladó") ? getBuyPrice(propertyPage) : getRentPrice(propertyPage));
        trackingData.setActive(true);
        return trackingData;
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

    private int getBuyPrice(Document propertyPage) {
        var price = propertyPage.getElementsByClass("parameters").first()
                .child(0).getElementsByTag("div").get(1)
                .getElementsByTag("span").first();
        float priceInMillions = Float.parseFloat(price.ownText().replace(",", ".").split(" ")[0]);
        return Math.round(priceInMillions * 1000000);
    }

    private int getRentPrice(Document propertyPage) {
        var price = propertyPage.getElementsByClass("parameters").first().getElementsByTag("div").get(1)
                .getElementsByClass("parameterValues").first().getElementsByTag("span").first()
                .ownText();
        return Math.round(Float.parseFloat(price.substring(0, price.indexOf(" ")).replace(",",".")) * 1000);
    }

    @SneakyThrows
    public PageResult parseSearchPage(String htmlPage) {
        var searchPage = Jsoup.parse(htmlPage);
        var dataLayer = getLegacyDataLayer(searchPage);
        return PageResult.builder()
                .properties(((List<Integer>) dataLayer.get("itemId")).stream().map(Long::valueOf).collect(Collectors.toList()))
                .hasNext(Integer.parseInt(dataLayer.get("numberOfItems").toString()) == DEFAULT_PAGE_SIZE)
                .nextPage(Integer.parseInt(dataLayer.get("page").toString()) + 1)
                .build();
    }

    @SneakyThrows
    private Map<String, Object> getDataLayer(Document page) {
        var dataLayerScriptElement = page.getElementsByTag("script").stream()
                .filter(s -> s.data().trim().startsWith("window['dataLayer']=["))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Data layer script tag not found!"));
        var dataLayerScript = dataLayerScriptElement.data().trim();
        var dataLayerJson = dataLayerScript.substring("window['dataLayer']=[".length(), dataLayerScript.indexOf("];if("));
        return objectMapper.readValue(dataLayerJson, Map.class);
    }

    @SneakyThrows
    private Map<String, Object> getLegacyDataLayer(Document page) {
        var dataLayerScriptElement = page.getElementsByTag("script").stream()
                .filter(s -> s.data().trim().startsWith("dataLayer.push("))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Data layer script tag not found!"));
        var dataLayerScript = dataLayerScriptElement.data().trim();
        var dataLayerJson = dataLayerScript.substring("dataLayer.push(".length(), dataLayerScript.length() - ");".length());
        return objectMapper.readValue(dataLayerJson, Map.class);
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
