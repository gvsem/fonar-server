package ru.georgii.fonarserver.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.georgii.fonarserver.user.User;
import ru.georgii.fonarserver.user.UserService;

@RestController
public class ConnectionController {

    @Autowired
    UserService userService;

    @Autowired
    FonarConfiguration config;


    ObjectMapper mapper = new ObjectMapper();

    @GetMapping(path = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
    ObjectNode about() {
        ObjectNode o = mapper.createObjectNode();

        o.put("server", "fonar-message-router-1.0-alpha.0");
        o.put("server_software_name", "Fonar Message Router");
        o.put("server_version", "1.0-alpha");

        o.put("api_spec", "FONAR");
        o.put("api_version", "1.0-alpha.0");

        o.put("server_name", config.serverName);
        o.put("salt", config.salt);
        o.put("socketUrl", config.socketUrl);

        return o;
    }

    @GetMapping(path = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    User register(@RequestParam("key") String key) {
        return userService.registerUser(key);
    }


}
