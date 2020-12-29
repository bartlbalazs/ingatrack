package hu.bartl.ingatrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.data.datastore.repository.config.EnableDatastoreRepositories;

@SpringBootApplication
@EnableDatastoreRepositories
public class IngatrackApplication {

	public static void main(String[] args) {
		SpringApplication.run(IngatrackApplication.class, args);
	}

}
