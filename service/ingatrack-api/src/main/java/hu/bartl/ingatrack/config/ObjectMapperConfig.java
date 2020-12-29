package hu.bartl.ingatrack.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@AllArgsConstructor
public class ObjectMapperConfig {

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Timestamp.class, new TimestampSerializer());
        objectMapper.registerModule(module);
    }

    public class TimestampSerializer extends StdSerializer<Timestamp> {

        public TimestampSerializer() {
            this(null);
        }

        public TimestampSerializer(Class<Timestamp> t) {
            super(t);
        }

        @Override
        public void serialize(Timestamp timestamp, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(timestamp.toString());
        }
    }
}
