package ru.georgii.fonarserver.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.georgii.fonarserver.auth.AuthService;
import ru.georgii.fonarserver.user.dto.UpdateUserProfile;

import javax.imageio.ImageIO;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;
import static ru.georgii.fonarserver.server.FonarConfiguration.API_VERSION_PREFIX;


@RestController
@SecurityRequirement(name = "fonarAuth")
@Tag(name = "user", description = "API to get users and update user profile")
@RequestMapping(API_VERSION_PREFIX)
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AuthService authService;

    @Operation(summary = "Get my user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping(path = "me", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> getMe(@AuthenticationPrincipal User me) {
        return getUserInfo(me.getId());
    }

    @Operation(summary = "Get user profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User was not found on this server")
    })
    @GetMapping(path = "user/info", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> getUserInfo(@Parameter(description = "Id of user", required = true)
                                     @RequestParam(value = "userId") Long userId) {

        Optional<User> u = userService.getUser(userId);
        if (u.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(u.get());
    }

    @Operation(summary = "Get list of users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users on this server",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class)))}),
    })
    @GetMapping(path = "users", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<User>> getUsers(@Parameter(description = "Number of messages to be retrieved", required = false)
                                        @RequestParam(value = "quantity", required = false, defaultValue = "20") Long quantity,
                                        @Parameter(description = "Offset of retrieved messages", required = false)
                                        @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset) {

        List<User> u = userService.getUsers(Math.min(20, quantity), offset);
        return ResponseEntity.ok(u);
    }

    @Operation(summary = "Update user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "List of users on this server",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class)))}),
            @ApiResponse(responseCode = "409", description = "User with proposed nickname already exists on this server"),
    })
    @PostMapping(path = "user/info", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> updateUserInfo(@Parameter(description = "UpdateUserProfile", required = true)
                                        @Valid @RequestBody UpdateUserProfile userDto,
                                        @AuthenticationPrincipal User me) {

        me.setFirstname(userDto.firstname);
        if (!Objects.equals(userService.userRepository.findByNickname(userDto.nickname).get().getId(), me.getId())) {
            return ResponseEntity.status(CONFLICT).body(null);
        } else {
            me.setNickname(userDto.nickname);
        }
        if (userDto.lastname != null) {
            me.setLastname(userDto.lastname);
        }
        if (userDto.bio != null) {
            me.setBio(userDto.bio);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(me));
    }


    @Operation(summary = "Get user avatar thumbnail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar thumbnail retrieved",
                    content = {@Content(mediaType = "image/jpeg")}),
            @ApiResponse(responseCode = "404", description = "No user was found by id")
    })
    @GetMapping(value = "user/photo", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> getPhoto(@Parameter(description = "Id of user", required = true)
                                           @RequestParam("uid") Long userId) {

        Optional<User> u = userService.getUser(userId);
        if (u.isEmpty()) {
            return ResponseEntity.status(NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(u.get().getAvatarBytes());
    }

    @Operation(summary = "Upload user avatar thumbnail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Avatar thumbnail was uploaded"),
            @ApiResponse(responseCode = "413", description = "Image is too large (> 100 KB)"),
            @ApiResponse(responseCode = "415", description = "Image is malformed"),
    })
    @PostMapping(value = "user/photo", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE} )
    public ResponseEntity<Void> uploadImage(@Parameter(description = "Id of user", required = true,
                                            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
                                            @RequestParam("image") MultipartFile file,
                                            @AuthenticationPrincipal User me) {

        if (file.getSize() > 100000) {
            return ResponseEntity.status(PAYLOAD_TOO_LARGE).body(null);
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(file.getBytes());
            ImageIO.read(bais);
            me.setAvatarBytes(file.getBytes());
            userService.saveUser(me);
        } catch (IOException e) {
            return ResponseEntity.status(UNSUPPORTED_MEDIA_TYPE).body(null);
        }

        return ResponseEntity.status(CREATED).body(null);

    }


}
