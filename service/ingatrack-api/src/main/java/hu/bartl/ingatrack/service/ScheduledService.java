package hu.bartl.ingatrack.service;

import com.google.common.base.Stopwatch;
import hu.bartl.ingatrack.config.ApplicationConfig;
import hu.bartl.ingatrack.exception.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduledService {

    private static final int PERIOD_LENGTH_IN_MINUTES = 8;

    private final ApplicationConfig applicationConfig;
    private final SubscriptionService subscriptionService;
    private final TrackingService trackingService;

    @PostConstruct
    public void onStart() {
        if (applicationConfig.isDoJobOnStart()) {
            log.info("Startup job enabled.");
            doJob();
        } else {
            log.info("Startup job disabled.");
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Async
    public void doJob() {
        log.info("Starting subscription tracking on schedule.");
        var stopwatch = Stopwatch.createStarted();
        log.info("Tracking properties by property subscription");
        subscriptionService.listPropertySubscriptions()
                .forEach(s -> trackingService.trackProperty(s.getPropertyId(), s.getId()));

        subscriptionService.listPropertySubscriptions()
                .forEach(s -> doIntermittentTracking(() -> trackingService.trackProperty(s.getPropertyId(), s.getId()), PERIOD_LENGTH_IN_MINUTES, stopwatch));

        log.info("Tracking properties by search subscription");
        subscriptionService.listSearchSubscriptions()
                .forEach(s -> doIntermittentTracking(() -> trackingService.trackSearch(s.getQuery(), s.getId()), PERIOD_LENGTH_IN_MINUTES, stopwatch));
        log.info("Scheduled subscription tracking finished.");
    }

    @SneakyThrows
    private void doIntermittentTracking(Runnable trackingMethod, int periodLengthInMinutes, Stopwatch stopwatch) {
        if (stopwatch.elapsed(TimeUnit.MINUTES) > periodLengthInMinutes) {
            log.info("Maximum consecutive work time exceeded. Wait for " + periodLengthInMinutes + " minutes.");
            Thread.sleep(periodLengthInMinutes * 1000 * 60);
            stopwatch.reset();
            stopwatch.start();
        }
        trackingMethod.run();
    }

    @Async
    public void trackProperty(String subscriptionId) {
        log.info("Requested tracking for Property with ID: " + subscriptionId);
        var subscription = subscriptionService.findPropertySubscription(subscriptionId)
                .orElseThrow(() -> new NotFoundException(subscriptionId));
        trackingService.trackProperty(subscription.getPropertyId(), subscription.getId());
    }

    @Async
    public void trackSearch(String subscriptionId) {
        log.info("Requested tracking for Search with ID: " + subscriptionId);
        var subscription = subscriptionService.findSearchSubscription(subscriptionId)
                .orElseThrow(() -> new NotFoundException(subscriptionId));
        trackingService.trackSearch(subscription.getQuery(), subscription.getId());
    }
}
