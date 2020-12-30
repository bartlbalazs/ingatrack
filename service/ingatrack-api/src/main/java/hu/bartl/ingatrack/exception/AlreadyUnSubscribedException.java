package hu.bartl.ingatrack.exception;

import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(code = HttpStatus.CONFLICT)
@ToString
public class AlreadyUnSubscribedException extends RuntimeException {

    private final Serializable id;

    public AlreadyUnSubscribedException(Serializable id) {
        super("Already unsubscribed from " + id + "");
        this.id = id;
    }
}
