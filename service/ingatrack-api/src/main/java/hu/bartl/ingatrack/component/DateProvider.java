package hu.bartl.ingatrack.component;

import com.google.cloud.Timestamp;
import org.springframework.stereotype.Component;

@Component
public class DateProvider {

    public Timestamp now() {
        return Timestamp.now();
    }
}
