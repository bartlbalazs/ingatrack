package hu.bartl.ingatrack.controller;

import hu.bartl.ingatrack.dto.PropertySubscriptionRequest;
import hu.bartl.ingatrack.dto.SearchSubscriptionRequest;
import hu.bartl.ingatrack.entity.subscription.PropertySubscription;
import hu.bartl.ingatrack.entity.subscription.SearchSubscription;
import hu.bartl.ingatrack.service.SubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/subscription", produces = APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping(value = "property")
    public void subscribeToProperty(PropertySubscriptionRequest request) {
        subscriptionService.subscribeToProperty(request.getId());
    }

    @DeleteMapping(value = "property")
    public void unsubscribeFromProperty(PropertySubscriptionRequest request) {
        subscriptionService.unsubscribeFromProperty(request.getId());
    }

    @GetMapping("property")
    public List<PropertySubscription> listPropertySubscriptions() {
        return subscriptionService.listPropertySubscriptions();
    }

    @PostMapping("search")
    public void subscribeToSearch(SearchSubscriptionRequest request) {
        subscriptionService.subscribeToSearch(request.getQuery());
    }

    @DeleteMapping("search")
    public void unsubscribeFromSearch(SearchSubscriptionRequest request) {
        subscriptionService.unsubscribeFromSearch(request.getQuery());
    }

    @GetMapping("search")
    public List<SearchSubscription> listSearchSubscriptions() {
        return subscriptionService.listSearchSubscriptions();
    }
}
