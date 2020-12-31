package hu.bartl.ingatrack.controller;

import hu.bartl.ingatrack.dto.PropertyTrackingTrigger;
import hu.bartl.ingatrack.service.ScheduledService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/scheduler", produces = APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class SchedulerController {

    private final ScheduledService scheduledService;

    @PostMapping(value = "property")
    public void schedulePropertyTracking(PropertyTrackingTrigger request) {
        scheduledService.trackProperty(request.getSubscriptionId());
    }

    @PostMapping(value = "search")
    public void scheduleSearchTracking(PropertyTrackingTrigger request) {
        scheduledService.trackSearch(request.getSubscriptionId());
    }

    @PostMapping(value = "all")
    public void scheduleTracking() {
        scheduledService.doJob();
    }
}
