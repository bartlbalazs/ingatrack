package hu.bartl.ingatrack.service;

import hu.bartl.ingatrack.config.ApplicationConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduledService {

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
    public void doJob() {
        log.info("Tracking properties by property subscription");
        subscriptionService.listPropertySubscriptions()
                .forEach(s -> trackingService.trackProperty(s.getPropertyId(), s.getId()));

        log.info("Tracking properties by search subscription");
        subscriptionService.listSearchSubscriptions()
                .forEach(s -> trackingService.trackSearch(s.getQuery(), s.getId()));
    }
}
