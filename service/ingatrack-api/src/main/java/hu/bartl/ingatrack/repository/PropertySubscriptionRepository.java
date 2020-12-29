package hu.bartl.ingatrack.repository;

import hu.bartl.ingatrack.entity.subscription.PropertySubscription;
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository;

public interface PropertySubscriptionRepository extends DatastoreRepository<PropertySubscription, String> {
}
