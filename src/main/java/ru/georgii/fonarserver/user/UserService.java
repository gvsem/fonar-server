package ru.georgii.fonarserver.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserService {

    @Autowired
    UserRepository userRepository;


    public boolean registerUser(String saltedGuid) {
        if (getUserBySaltedGuid(saltedGuid).isPresent()) {
            return false;
        }
        User u = new User(saltedGuid);
        userRepository.save(u);
        return true;
    }

    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserBySaltedGuid(String uid) {
        return userRepository.findByUid(uid);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsers(Long quantity, Long offset) {
        return userRepository.getUsers(quantity, offset);
    }

    public User saveUser(User u) {
        return userRepository.save(u);
    }

    private Long getUsersCount() {
        return userRepository.count();
    }

}
