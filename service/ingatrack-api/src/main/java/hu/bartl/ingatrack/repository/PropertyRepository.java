package hu.bartl.ingatrack.repository;

import hu.bartl.ingatrack.entity.Property;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PropertyRepository extends CrudRepository<Property, String> {

    @Query(value = "SELECT p FROM Property p LEFT JOIN FETCH p.trackingData where p.propertyId = :propertyId")
    Optional<Property> findByPropertyId(@Param("propertyId") int propertyId);
}
