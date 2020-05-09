package hu.bartl.ingatrack.repository;

import hu.bartl.ingatrack.entity.subscription.SearchSubscription;
import org.springframework.data.repository.CrudRepository;

public interface SearchSubscriptionRepository extends CrudRepository<SearchSubscription, String> {
}
