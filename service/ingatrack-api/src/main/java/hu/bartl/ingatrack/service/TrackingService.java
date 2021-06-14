package hu.bartl.ingatrack.service;

import hu.bartl.ingatrack.repository.TrackingDataRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class TrackingService {

    private final TrackingDataRepository trackingDataRepository;
    private final TrackingDataService trackingDataService;

    public void trackProperty(long propertyId, String requestSource) {
        log.info("Tracking property " + propertyId + " initiated by subscription: " + requestSource);
        var trackingData = trackingDataService.fetchTrackingData(propertyId);
        trackingData.setInsertedBy(requestSource);
        trackingDataRepository.save(trackingData);
    }

    public void trackSearch(String query, String requestSource) {
        log.info("Tracking query " + query + " initiated by subscription: " + requestSource);
        var trackingData = trackingDataService.fetchPropertyIds(query)
                .stream()
                .map(trackingDataService::fetchTrackingData)
                .map(t -> t.toBuilder().insertedBy(requestSource).build())
                .collect(Collectors.toList());
        trackingDataRepository.save(trackingData);
        log.info("Tracking query " + query + " finished.");
    }
}
