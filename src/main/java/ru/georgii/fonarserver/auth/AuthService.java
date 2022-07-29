package ru.georgii.fonarserver.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.georgii.fonarserver.user.User;
import ru.georgii.fonarserver.user.UserRepository;

import java.util.List;

@Component
public class AuthService {

    @Autowired
    UserRepository userRepository;

    public User authenticateBySaltedGuid(String saltedGuid) throws AuthorizationException {
        if (saltedGuid == null) {
            throw new AuthorizationException("No saltedGuid found.");
        }
        List<User> users = this.userRepository.findByUid(saltedGuid);
        if (users.size() == 0) {
            throw new AuthorizationException("Authentication failed.");
        }
        return users.get(0);
    }

}
