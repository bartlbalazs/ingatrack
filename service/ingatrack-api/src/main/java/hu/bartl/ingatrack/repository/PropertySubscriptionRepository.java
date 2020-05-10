package hu.bartl.ingatrack.repository;

import hu.bartl.ingatrack.entity.subscription.PropertySubscription;
import org.springframework.data.repository.CrudRepository;

public interface PropertySubscriptionRepository extends CrudRepository<PropertySubscription, String> {
}
