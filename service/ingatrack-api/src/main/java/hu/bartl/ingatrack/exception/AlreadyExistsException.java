package hu.bartl.ingatrack.exception;

import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
@ToString
public class AlreadyExistsException extends RuntimeException {

    private Object id;

    public AlreadyExistsException(Object id) {
        super("Id " + id + " already exists.");
        this.id = id;
    }
}
