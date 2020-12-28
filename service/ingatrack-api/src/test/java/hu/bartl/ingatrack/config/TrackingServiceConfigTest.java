package hu.bartl.ingatrack.config;

import hu.bartl.ingatrack.TrackingService;
import hu.bartl.ingatrack.component.DateProvider;
import hu.bartl.ingatrack.repository.PropertyRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
class TrackingServiceConfigTest {

    @Autowired
    static ClientAndServer mockServer;

    @Autowired
    PropertyRepository propertyRepository;
    @MockBean
    DateProvider dateProvider;

    @Autowired
    TrackingService underTest;

    @BeforeAll
    static void setUp() {
        mockServer = startClientAndServer(8081);
    }

    @Test
    @SneakyThrows
    void testFetchTrackingDataForProperty() {
        var propertyId = 30977395;
        var sampleFile = new ClassPathResource("sample/" + propertyId + ".html").getFile();
        var sample = Files.readAllLines(Paths.get(sampleFile.getAbsolutePath())).stream().collect(Collectors.joining());

        mockServer.when(
                request().withMethod(HttpMethod.GET.name())
                        .withPath("/" + propertyId))
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(sample));

        when(dateProvider.now()).thenReturn(Instant.ofEpochSecond(1));

        underTest.trackProperty(propertyId);

        var property = propertyRepository.findByPropertyId(propertyId).get();
        assertThat(property.getPropertyId(), is(propertyId));
        assertThat(property.getCity(), is("Szeged"));
        assertThat(property.getBuiltAfter(), is(1981));
        assertThat(property.getBuiltBefore(), is(2000));
        assertThat(property.getSquareFootage(), is(72));
        assertThat(property.isPanel(), is(true));

        assertThat(property.getTrackingData().size(), is(1));
        assertThat(new ArrayList<>(property.getTrackingData()).get(0).isActive(), is(true));
        assertThat(new ArrayList<>(property.getTrackingData()).get(0).getCreatedAt(), is(dateProvider.now()));
        assertThat(new ArrayList<>(property.getTrackingData()).get(0).getPrice(), is(19900000));
    }

    @AfterAll
    static void tearDown() {
        mockServer.stop();
    }
}