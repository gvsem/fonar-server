package ru.georgii.fonarserver.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.coyote.Response;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.georgii.fonarserver.dialog.Message;
import ru.georgii.fonarserver.user.User;
import ru.georgii.fonarserver.user.UserService;

@RestController
public class ConnectionController {

    @Autowired
    UserService userService;

    @Autowired
    FonarConfiguration config;

    @Operation(summary = "Get server configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration is retrieved",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = FonarConfiguration.class))}),
    })
    @GetMapping(path = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
    FonarConfiguration about() {
        return config;
    }

    @Operation(summary = "Register on this server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User is already registered",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "201", description = "User has been registered",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "403", description = "Registration is not available"),
    })
    @GetMapping(path = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> register(@Parameter(description = "Secret randomly generated sha-512 key " +
                                    "in hex containing server salt and client GUID", required = true)
                                    @RequestParam("key") @Length(min = 128, max = 128) String key) {
        if (userService.registerUser(key)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.getUserBySaltedGuid(key).get());
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserBySaltedGuid(key).get());
        }
    }


}
