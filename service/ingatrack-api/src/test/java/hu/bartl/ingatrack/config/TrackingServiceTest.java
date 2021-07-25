package hu.bartl.ingatrack.config;

import com.google.cloud.Timestamp;
import hu.bartl.ingatrack.component.DateProvider;
import hu.bartl.ingatrack.repository.TrackingDataRepository;
import hu.bartl.ingatrack.service.TrackingService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
class TrackingServiceTest {

    static final String REQUEST_SOURCE = "TEST";

    static final Timestamp CURRENT_TIMESTAMP = Timestamp.now();

    @Autowired
    static ClientAndServer mockServer;

    @Autowired
    TrackingDataRepository trackingDataRepository;

    @MockBean
    DateProvider dateProvider;

    @Autowired
    TrackingService underTest;

    @BeforeAll
    static void beforeAll() {
        mockServer = startClientAndServer(8081);
    }

    @BeforeEach
    void beforeEach() {
        when(dateProvider.now()).thenReturn(CURRENT_TIMESTAMP);
    }

    @Test
    @SneakyThrows
    void shouldTrackPropertyWithActiveAdvertisement() {
        var propertyId = 32341262L;

        prepareMockServerForProperty(propertyId, HttpStatusCode.OK_200);

        underTest.trackProperty(propertyId, REQUEST_SOURCE);

        var trackingData = trackingDataRepository.findLatestByPropertyId(propertyId).get();
        assertTrue(trackingData.isActive());
        assertThat(trackingData.getListingType(), is("Eladó"));
        assertThat(trackingData.getCreatedAt(), is(CURRENT_TIMESTAMP));

        var property = trackingData.getProperty();
        assertThat(property.getPropertyId(), is(propertyId));
        assertThat(property.getPropertyType(), is("ház"));
        assertThat(property.getPropertySubType(), is("családi ház"));
        assertThat(property.getCounty(), is("Pest"));
        assertThat(property.getCity(), is("Sződliget"));
        assertThat(property.getStreet(), is("Tompa utca"));
        assertThat(property.getSquareMeters(), is(57));
    }

    @Test
    @SneakyThrows
    void shouldTrackPropertyForRentWithActiveAdvertisement() {
        var propertyId = 23778830L;

        prepareMockServerForProperty(propertyId, HttpStatusCode.OK_200);

        underTest.trackProperty(propertyId, REQUEST_SOURCE);

        var trackingData = trackingDataRepository.findLatestByPropertyId(propertyId).get();
        assertTrue(trackingData.isActive());
        assertThat(trackingData.getListingType(), is("Kiadó"));
        assertThat(trackingData.getCreatedAt(), is(CURRENT_TIMESTAMP));

        var property = trackingData.getProperty();
        assertThat(property.getPropertyId(), is(propertyId));
        assertThat(property.getPropertyType(), is("lakás"));
        assertThat(property.getPropertySubType(), is("tégla"));
        assertThat(property.getCounty(), is("Győr-Moson-Sopron"));
        assertThat(property.getCity(), is("Győr"));
        assertThat(property.getStreet(), is("Liszt Ferenc utca"));
        assertThat(property.getConditionType(), is("újszerű"));
        assertThat(property.getSquareMeters(), is(44));
    }

    @Test
    @SneakyThrows
    void shouldTrackPropertyWithInactiveAdvertisement() {
        var propertyId = 22619523L;

        prepareMockServerForProperty(propertyId, HttpStatusCode.NOT_FOUND_404);

        underTest.trackProperty(propertyId, REQUEST_SOURCE);

        var trackingData = trackingDataRepository.findLatestByPropertyId(propertyId).get();
        assertFalse(trackingData.isActive());
        assertThat(trackingData.getProperty().getPropertyId(), is(propertyId));
        assertEquals(CURRENT_TIMESTAMP, trackingData.getCreatedAt());
    }

    @Test
    @SneakyThrows
    void shouldTrackPropertiesOfSearchSubscription() {
        var query = "sample-query";

        prepareMockServerForSearch(query, 1);
        prepareMockServerForSearch(query, 2);

        prepareMockServerForProperty(22619523, HttpStatusCode.OK_200);
        prepareMockServerForProperty(32297628, HttpStatusCode.OK_200);
        prepareMockServerForProperty(32341262, HttpStatusCode.OK_200);

        underTest.trackSearch(query, "TEST");

        var t1 = trackingDataRepository.findLatestByPropertyId(22619523).get();
        assertEquals(CURRENT_TIMESTAMP, t1.getCreatedAt());
        var t2 = trackingDataRepository.findLatestByPropertyId(32297628).get();
        assertEquals(CURRENT_TIMESTAMP, t2.getCreatedAt());
        var t3 = trackingDataRepository.findLatestByPropertyId(32341262).get();
        assertEquals(CURRENT_TIMESTAMP, t3.getCreatedAt());
    }

    @SneakyThrows
    private void prepareMockServerForProperty(long propertyId, HttpStatusCode statusCode) {
        prepareMockServer(propertyId, "", statusCode, null);
    }

    @SneakyThrows
    private void prepareMockServerForSearch(String query, int page) {
        prepareMockServer(query, "lista/", HttpStatusCode.OK_200, Parameter.param("page", String.valueOf(page)));
    }

    @SneakyThrows
    private void prepareMockServer(Object sampleId, String context, HttpStatusCode statusCode, Parameter parameter) {
        var path = "sample/" + sampleId;
        if (parameter != null) {
            path += "?" + parameter.getName().getValue() + "=" + parameter.getValues().get(0).getValue();
        }
        var sampleFile = new ClassPathResource(path + ".html").getFile();
        var sample = Files.readAllLines(Paths.get(sampleFile.getAbsolutePath())).stream().collect(Collectors.joining());

        var httpRequest = request()
                .withMethod(HttpMethod.GET.name())
                .withPath("/" + context + sampleId);
        if (parameter != null) {
            httpRequest = httpRequest.withQueryStringParameter(parameter);
        }
        mockServer.when(
                httpRequest)
                .respond(response()
                        .withStatusCode(statusCode.code())
                        .withBody(sample, UTF_8));

    }

    @AfterEach
    void afterEach() {
        mockServer.reset();
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }
}