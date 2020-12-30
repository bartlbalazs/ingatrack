package hu.bartl.ingatrack.service;

import hu.bartl.ingatrack.entity.subscription.PropertySubscription;
import hu.bartl.ingatrack.entity.subscription.SearchSubscription;
import hu.bartl.ingatrack.exception.AlreadyExistsException;
import hu.bartl.ingatrack.exception.AlreadyUnSubscribedException;
import hu.bartl.ingatrack.exception.NotFoundException;
import hu.bartl.ingatrack.repository.SubscriptionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public void subscribeToProperty(long propertyId) {
        subscriptionRepository.findPropertySubscription(propertyId).ifPresent(p -> {
            if (p.isActive()) {
                throw new AlreadyExistsException(propertyId);
            }
        });

        PropertySubscription subscription = PropertySubscription.builder().propertyId(propertyId).build();
        subscriptionRepository.save(subscription);
        log.info("Subscription created {}", subscription);
    }

    public void unsubscribeFromProperty(long propertyId) {
        var subscription = subscriptionRepository.findPropertySubscription(propertyId).orElseThrow(() -> {
            throw new NotFoundException(propertyId);
        });

        if (!subscription.isActive()) {
            throw new AlreadyUnSubscribedException(propertyId);
        }

        subscription.setActive(false);
        subscriptionRepository.save(subscription);
        log.info("Subscription inactivated {}", subscription);
    }

    public List<PropertySubscription> listPropertySubscriptions() {
        return subscriptionRepository.listActivePropertySubscriptions();
    }

    public void subscribeToSearch(String query) {
        subscriptionRepository.findSearchSubscription(query).ifPresent(s -> {
            if (s.isActive()) {
                throw new AlreadyExistsException(query);
            }
        });

        SearchSubscription subscription = SearchSubscription.builder().query(query).build();
        subscriptionRepository.save(subscription);
        log.info("Subscription created {}", subscription);
    }

    public void unsubscribeFromSearch(String query) {
        var subscription = subscriptionRepository.findSearchSubscription(query).orElseThrow(() -> {
            throw new NotFoundException(query);
        });

        if (!subscription.isActive()) {
            throw new AlreadyUnSubscribedException(query);
        }

        subscription.setActive(false);
        subscriptionRepository.save(subscription);
        log.info("Subscription inactivated {}", subscription);
    }

    public List<SearchSubscription> listSearchSubscriptions() {
        return subscriptionRepository.listActiveSearchSubscriptions();
    }
}
