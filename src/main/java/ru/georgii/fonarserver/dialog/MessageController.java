package ru.georgii.fonarserver.dialog;

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
import ru.georgii.fonarserver.auth.AuthService;
import ru.georgii.fonarserver.dialog.dto.DialogDto;
import ru.georgii.fonarserver.dialog.dto.SendMessageDto;
import ru.georgii.fonarserver.gateway.MessageGateway;
import ru.georgii.fonarserver.user.User;
import ru.georgii.fonarserver.user.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static ru.georgii.fonarserver.server.FonarConfiguration.API_VERSION_PREFIX;


@RestController
@SecurityRequirement(name = "fonarAuth")
@Tag(name = "message", description = "API to send and retrieve messages")
@RequestMapping(API_VERSION_PREFIX)
public class MessageController {

    @Autowired
    AuthService authService;

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    MessageGateway messageGateway;

    @Operation(summary = "Get list of dialogs sorted in date descending")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of dialogs",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DialogDto.class)))}),
    })
    @GetMapping(path = "dialogs", produces = MediaType.APPLICATION_JSON_VALUE)
    List<DialogDto> getDialogs(
                               @Parameter(description = "Number of messages to be retrieved")
                               @RequestParam(value = "quantity", required = false, defaultValue = "20") Long quantity,
                               @Parameter(description = "Offset of retrieved messages", required = false)
                               @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset,
                               @AuthenticationPrincipal User me) {
        return this.messageService.getDialogs(me, Math.min(20, quantity), offset);
    }

    @Operation(summary = "Get messages within dialog in descending order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of dialogs",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Message.class)))}),
            @ApiResponse(responseCode = "404", description = "No conversation was found"),
    })
    @GetMapping(path = "dialog", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Message>> getDialog(@Parameter(description = "Id of conversation (user id)")
                            @RequestParam(value = "userId") Long userId,
                            @Parameter(description = "Number of messages to be retrieved")
                            @RequestParam(value = "quantity", required = false, defaultValue = "20") Long quantity,
                            @Parameter(description = "Offset of retrieved messages", required = false)
                            @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset,
                            @AuthenticationPrincipal User me) {

        Optional<User> to = this.userService.getUser(userId);
        if (to.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(this.messageService.getDialog(me, to.get(), Math.min(20, quantity), offset));
    }

    @Operation(summary = "Send message to conversation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message has been successfully sent",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Message.class))}),
            @ApiResponse(responseCode = "404", description = "No conversation was found"),
            @ApiResponse(responseCode = "409", description = "This user does not accept messages"),
    })
    @PostMapping(path = "message")
    ResponseEntity<Message> postMessage(@Parameter(description = "Id of conversation (user id) message is sent to")
                                        @RequestParam(value = "userId") Long userId,
                                        @Parameter(description = "Message dto")
                                        @Valid @RequestBody SendMessageDto message,
                                        @AuthenticationPrincipal User me) {

        Optional<User> to = this.userService.getUser(userId);
        if (to.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.messageService.sendMessage(me, to.get(), new Message(message.getText())));
    }


    @Operation(summary = "Mark message as seen")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message has been successfully marked as seen"),
            @ApiResponse(responseCode = "404", description = "Message was not found"),
            @ApiResponse(responseCode = "409", description = "Message is already seen"),
    })
    @PutMapping(path = "message/seen")
    ResponseEntity<Void> seenMessage(@Parameter(description = "Id of conversation (user id)")
                                     @RequestParam(value = "userId") Long userId,
                                     @Parameter(description = "Id of message to mark as seen")
                                     @RequestParam(value = "messageId") Long messageId,
                                     @AuthenticationPrincipal User me) {

        Optional<User> to = this.userService.getUser(userId);
        if (to.isEmpty()) {
            throw new RuntimeException("User with " + userId + " not found on this server.");
        }

        MessageService.SEEN_METHOD_STATUS response = this.messageService.markAsSeen(me, to.get().getId(), messageId);
        if (response == MessageService.SEEN_METHOD_STATUS.OK) {
            return ResponseEntity.ok(null);
        } else if (response == MessageService.SEEN_METHOD_STATUS.ALREADY_SEEN) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } else if (response == MessageService.SEEN_METHOD_STATUS.NO_MESSAGE_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
        }
    }


}
