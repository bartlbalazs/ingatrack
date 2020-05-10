package hu.bartl.ingatrack.controller;

import hu.bartl.ingatrack.dto.PropertySubscriptionRequest;
import hu.bartl.ingatrack.dto.SearchSubscriptionRequest;
import hu.bartl.ingatrack.entity.subscription.PropertySubscription;
import hu.bartl.ingatrack.entity.subscription.SearchSubscription;
import hu.bartl.ingatrack.repository.PropertySubscriptionRepository;
import hu.bartl.ingatrack.repository.SearchSubscriptionRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/v1/subscription")
@AllArgsConstructor
public class SubscriptionController {

    private final PropertySubscriptionRepository propertySubscriptionRepository;
    private final SearchSubscriptionRepository searchSubscriptionRepository;

    @PostMapping("property")
    public void subscribeToProperty(PropertySubscriptionRequest request) {
        PropertySubscription subscription = PropertySubscription.builder().propertyId(request.getId()).build();
        propertySubscriptionRepository.save(subscription);
    }

    @GetMapping("property")
    public List<PropertySubscription> listPropertySubscriptions() {
        return StreamSupport.stream(propertySubscriptionRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @PostMapping("search")
    public void subscribeToSearch(SearchSubscriptionRequest request) {
        SearchSubscription subscription = SearchSubscription.builder().query(request.getQuery()).build();
        searchSubscriptionRepository.save(subscription);
    }

    @GetMapping("search")
    public List<SearchSubscription> listSearchSubscriptions() {
        return StreamSupport.stream(searchSubscriptionRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }
}
