package hu.bartl.ingatrack.exception;

import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(code = HttpStatus.CONFLICT)
@ToString
public class AlreadyExistsException extends RuntimeException {

    private final Serializable id;

    public AlreadyExistsException(Serializable id) {
        super("Id " + id + " already exists.");
        this.id = id;
    }
}
