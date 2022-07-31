package ru.georgii.fonarserver.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.georgii.fonarserver.user.User;

import java.util.ArrayList;
import java.util.Collections;

@Component
public class CustomTokenAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    AuthService authService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String customToken = (String) authentication.getCredentials();

        try {
            User me = authService.authenticateBySaltedGuid(customToken);
            return new AbstractAuthenticationToken(Collections.singleton(new SimpleGrantedAuthority("USER"))) {
                @Override
                public Object getCredentials() {
                    return "";
                }

                @Override
                public Object getPrincipal() {
                    return me;
                }
            };
        } catch (AuthorizationException e) {
            throw new AccessDeniedException("Invalid token");
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.equals(authentication);
    }

}
