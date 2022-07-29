package ru.georgii.fonarserver.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.georgii.fonarserver.auth.AuthService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;


@RestController
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuthService authService;

    ObjectMapper mapper = new ObjectMapper();

    @GetMapping(path = "/v1.0/me", produces = MediaType.APPLICATION_JSON_VALUE)
    ObjectNode getMe(@RequestHeader("Authorization") String saltedGuid) {
        User me = null;
        try {
            me = authService.authenticateBySaltedGuid(saltedGuid);
            return getUserInfo(me.getId());
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "No valid authorization");
        }
    }

    @GetMapping(path = "/v1.0/user/info", produces = MediaType.APPLICATION_JSON_VALUE)
    ObjectNode getUserInfo(@RequestParam(value = "userId") Long userId) {

        Optional<User> u = userService.getUser(userId);
        if (u.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Unable to find resource");
        }

        return u.get().toObjectNode(mapper);
    }

    @GetMapping(path = "/v1.0/users", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ObjectNode> getUsers(@RequestParam(value = "quantity", required = false, defaultValue = "20") Long quantity,
                              @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset) {

        List<User> u = userService.getUsers(Math.min(20, quantity), offset);
        if (u.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Unable to find resource");
        }
        List<ObjectNode> dtos = new ArrayList<>();

        for (User user : u) {
            dtos.add(user.toObjectNode(mapper));
        }

        return dtos;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/v1.0/user/info", produces = MediaType.APPLICATION_JSON_VALUE)
    ObjectNode updateUserInfo(@RequestBody Map<String, Object> payload,
                              @RequestHeader("Authorization") String saltedGuid) {
        User me;
        try {
            me = authService.authenticateBySaltedGuid(saltedGuid);
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "No valid authorization");
        }

        if (payload.containsKey("firstname")) {
            me.setFirstname((String) payload.get("firstname"));
        }

        if (payload.containsKey("lastname")) {
            me.setLastname((String) payload.get("lastname"));
        }

        if (payload.containsKey("bio")) {
            me.setBio((String) payload.get("bio"));
        }

        if (payload.containsKey("nickname")) {
            me.setNickname((String) payload.get("nickname"));
        }

        userService.saveUser(me);

        return getUserInfo(me.getId());
    }


    @GetMapping(value = "/v1.0/user/photo", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public byte[] getPhoto(@RequestParam("uid") Long userId) {

        Optional<User> u = userService.getUser(userId);
        if (u.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Unable to find resource");
        }

        return u.get().getPhotoByteArray();
    }

    @ResponseStatus(CREATED)
    @PostMapping("/v1.0/user/photo")
    public void uploadImage(@RequestHeader("Authorization") String saltedGuid,
                            @RequestParam("image") MultipartFile file)
            throws IOException {

        User me;
        try {
            me = authService.authenticateBySaltedGuid(saltedGuid);
        } catch (Exception e) {
            throw new ResponseStatusException(UNAUTHORIZED, "No valid authorization");
        }

        if (file.getSize() > 100000) {
            throw new ResponseStatusException(BAD_REQUEST, "Too big image (> 100.000 bytes) ");
        }

        me.setPhotoByteArray(file.getBytes());

        userService.saveUser(me);

    }


}
