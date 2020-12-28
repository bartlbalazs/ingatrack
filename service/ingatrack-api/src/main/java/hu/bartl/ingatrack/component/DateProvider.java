package hu.bartl.ingatrack.component;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DateProvider {

    public Instant now() {
        return Instant.now();
    }
}
