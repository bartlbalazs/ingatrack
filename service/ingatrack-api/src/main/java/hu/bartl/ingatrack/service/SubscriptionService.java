package hu.bartl.ingatrack.service;

import hu.bartl.ingatrack.entity.subscription.PropertySubscription;
import hu.bartl.ingatrack.entity.subscription.SearchSubscription;
import hu.bartl.ingatrack.exception.AlreadyExistsException;
import hu.bartl.ingatrack.exception.NotFoundException;
import hu.bartl.ingatrack.repository.PropertySubscriptionRepository;
import hu.bartl.ingatrack.repository.SearchSubscriptionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
@Slf4j
public class SubscriptionService {

    private final PropertySubscriptionRepository propertyRepository;
    private final SearchSubscriptionRepository searchRepository;

    public void subscribeToProperty(long propertyId) {
        propertyRepository.getOneByPropertyId(propertyId).ifPresent(p -> {
            if (p.isActive()) {
                throw new AlreadyExistsException(propertyId);
            }
        });

        PropertySubscription subscription = PropertySubscription.builder().propertyId(propertyId).build();
        propertyRepository.save(subscription);
        log.info("Subscription created {}", subscription);
    }

    public void unsubscribeFromProperty(long propertyId) {
        var subscription = propertyRepository.getOneByPropertyId(propertyId).orElseThrow(() -> {
            throw new NotFoundException(propertyId);
        });

        subscription.setActive(false);
        propertyRepository.save(subscription);
        log.info("Subscription inactivated {}", subscription);
    }

    public List<PropertySubscription> listPropertySubscriptions() {
        return StreamSupport.stream(propertyRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public void subscribeToSearch(String query) {
        searchRepository.getOneByQuery(query).ifPresent(s -> {
            if (s.isActive()) {
                throw new AlreadyExistsException(query);
            }
        });

        SearchSubscription subscription = SearchSubscription.builder().query(query).build();
        searchRepository.save(subscription);
        log.info("Subscription created {}", subscription);
    }

    public void unsubscribeFromSearch(String query) {
        var subscription = searchRepository.getOneByQuery(query).orElseThrow(() -> {
            throw new NotFoundException(query);
        });

        subscription.setActive(false);
        searchRepository.save(subscription);
        log.info("Subscription inactivated {}", subscription);
    }

    public List<SearchSubscription> listSearchSubscriptions() {
        return StreamSupport.stream(searchRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }
}
