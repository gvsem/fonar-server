package ru.georgii.fonarserver.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Authorization failed.")
public class AuthorizationException extends Exception {

    public AuthorizationException(String text) {
        super(text);
    }

}
