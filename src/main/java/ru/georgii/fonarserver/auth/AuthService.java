package ru.georgii.fonarserver.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.georgii.fonarserver.user.User;
import ru.georgii.fonarserver.user.UserRepository;

import java.util.List;
import java.util.Optional;

@Component
public class AuthService {

    @Autowired
    UserRepository userRepository;

    public User authenticateBySaltedGuid(String saltedGuid) throws AuthorizationException {
        if (saltedGuid == null) {
            throw new AuthorizationException("No saltedGuid found.");
        }
        Optional<User> user = this.userRepository.findByUid(saltedGuid);
        if (user.isEmpty()) {
            throw new AuthorizationException("Authentication failed.");
        }
        return user.get();
    }

}
