package hu.bartl.ingatrack.exception;

import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
@ToString
public class NotFoundException extends RuntimeException {

    private Object id;

    public NotFoundException(Object id) {
        super("Entity with id " + id + " not found.");
        this.id = id;
    }
}
