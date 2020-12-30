package hu.bartl.ingatrack.exception;

import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
@ToString
public class NotFoundException extends RuntimeException {

    private final Serializable id;

    public NotFoundException(Serializable id) {
        super("Entity with id " + id + " not found.");
        this.id = id;
    }
}
