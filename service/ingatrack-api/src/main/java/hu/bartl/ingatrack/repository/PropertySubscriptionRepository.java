package hu.bartl.ingatrack.repository;

import hu.bartl.ingatrack.entity.subscription.PropertySubscription;
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository;

import java.util.Optional;

public interface PropertySubscriptionRepository extends DatastoreRepository<PropertySubscription, String> {

    Optional<PropertySubscription> getOneByPropertyId(long propertyId);
}
