package hu.bartl.ingatrack.config;

import com.google.cloud.Timestamp;
import hu.bartl.ingatrack.component.DateProvider;
import hu.bartl.ingatrack.repository.TrackingDataRepository;
import hu.bartl.ingatrack.service.TrackingService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
        var propertyId = 30977395L;

        prepareMockServer(propertyId, HttpStatusCode.OK_200);

        underTest.trackProperty(propertyId, REQUEST_SOURCE);

        var trackingData = trackingDataRepository.findLatestByPropertyId(propertyId).get();
        assertTrue(trackingData.isActive());
        assertEquals(CURRENT_TIMESTAMP, trackingData.getCreatedAt());

        var property = trackingData.getProperty();
        assertThat(property.getPropertyId(), is(propertyId));
        assertThat(property.getCity(), is("Szeged"));
        assertThat(property.getBuiltAfter(), is(1981));
        assertThat(property.getBuiltBefore(), is(2000));
        assertThat(property.getSquareMeters(), is(72));
        assertThat(property.getPanel(), is(true));
    }

    @Test
    @SneakyThrows
    void shouldTrackPropertyWithInactiveAdvertisement() {
        var propertyId = 318110356L;

        prepareMockServer(propertyId, HttpStatusCode.NOT_FOUND_404);

        underTest.trackProperty(propertyId, REQUEST_SOURCE);

        var trackingData = trackingDataRepository.findLatestByPropertyId(propertyId).get();
        assertFalse(trackingData.isActive());
        assertThat(trackingData.getProperty().getPropertyId(), is(propertyId));
        assertNull(trackingData.getProperty().getPanel());
        assertEquals(CURRENT_TIMESTAMP, trackingData.getCreatedAt());
    }

    @SneakyThrows
    private void prepareMockServer(long propertyId, HttpStatusCode statusCode) {
        var sampleFile = new ClassPathResource("sample/" + propertyId + ".html").getFile();
        var sample = Files.readAllLines(Paths.get(sampleFile.getAbsolutePath())).stream().collect(Collectors.joining());

        mockServer.when(
                request().withMethod(HttpMethod.GET.name())
                        .withPath("/" + propertyId))
                .respond(response()
                        .withStatusCode(statusCode.code())
                        .withBody(sample));
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }
}