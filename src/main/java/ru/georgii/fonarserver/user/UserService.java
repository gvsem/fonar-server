package ru.georgii.fonarserver.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User registerUser(String saltedGuid) {
        List<User> existingUsers = userRepository.findByUid(saltedGuid);
        if (existingUsers.size() != 0) {
            return existingUsers.get(0);
        }
        User u = new User(saltedGuid);
        userRepository.save(u);
        return u;
    }

    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
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
