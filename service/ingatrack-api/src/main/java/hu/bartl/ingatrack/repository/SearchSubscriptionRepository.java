package hu.bartl.ingatrack.repository;

import hu.bartl.ingatrack.entity.subscription.SearchSubscription;
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository;

public interface SearchSubscriptionRepository extends DatastoreRepository<SearchSubscription, String> {
}
