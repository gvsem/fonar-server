package ru.georgii.fonarserver.dialog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.georgii.fonarserver.auth.AuthService;
import ru.georgii.fonarserver.dialog.dto.DialogDto;
import ru.georgii.fonarserver.gateway.MessageGateway;
import ru.georgii.fonarserver.user.User;
import ru.georgii.fonarserver.user.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1.0/")
public class MessageController {

    @Autowired
    AuthService authService;

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    MessageGateway messageGateway;


    @GetMapping(path = "dialogs", produces = MediaType.APPLICATION_JSON_VALUE)
    List<DialogDto> getDialogs(//@RequestBody Map<String, Object> payload,
                               @RequestParam(value = "quantity", required = false, defaultValue = "20") Long quantity,
                               @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset,
                               @RequestHeader("Authorization") String saltedGuid) {
        User me = null;
        try {
            me = authService.authenticateBySaltedGuid(saltedGuid);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return this.messageService.getDialogs(me, Math.min(20, quantity), offset);

//        Set<Long> userIds = new HashSet<>();
//        for (DialogDto d : dialogs) {
//            userIds.add(d.user.getId());
//        }
//
//        List<User> users = this.userService.getUsers();
//        for (User u : users) {
//            if (u.getId() == me.getId()) {
//                continue;
//            }
//            if (userIds.contains(u.getId())) {
//                continue;
//            }
//            DialogDto d = new DialogDto();
//            d.lastMessageIsToMe = false;
//            d.unreadMessages = 0L;
//            d.lastMessage = null;
//            d.user = u;
//            dialogs.add(d);
//        }
//
//        return dialogs;
    }

    @GetMapping(path = "dialog", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Message> getDialog(@RequestParam(value = "userId") Long userId,
                            @RequestParam(value = "quantity", required = false, defaultValue = "20") Long quantity,
                            @RequestParam(value = "offset", required = false, defaultValue = "0") Long offset,
                            @RequestHeader("Authorization") String saltedGuid) {
        User me = null;
        try {
            me = authService.authenticateBySaltedGuid(saltedGuid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Optional<User> to = this.userService.getUser(userId);
        if (to.isEmpty()) {
            throw new RuntimeException("User with " + userId + " not found on this server.");
        }

        return this.messageService.getDialog(me, to.get(), Math.min(20, quantity), offset);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "message")
    Message postMessage(@RequestParam(value = "userId") Long userId,
                        @RequestBody Map<String, Object> payload,
                        @RequestHeader("Authorization") String saltedGuid) {
        User me = null;
        try {
            me = authService.authenticateBySaltedGuid(saltedGuid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Optional<User> to = this.userService.getUser(userId);
        if (to.isEmpty()) {
            throw new RuntimeException("User with " + userId + " not found on this server.");
        }

        if (!payload.containsKey("type")) {
            throw new RuntimeException("Message type is not provided.");
        }

        if (!payload.get("type").equals("plain")) {
            throw new RuntimeException("Only plain text messages are supported.");
        }

        if (!payload.containsKey("text")) {
            throw new RuntimeException("Message text must be provided.");
        }

        return this.messageService.sendMessage(me, to.get(), new Message((String) payload.get("text")));
    }


    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "message")
    void seenMessage(@RequestParam(value = "userId") Long userId,
                     @RequestParam(value = "messageId") Long messageId,
                     @RequestHeader("Authorization") String saltedGuid) {
        User me = null;
        try {
            me = authService.authenticateBySaltedGuid(saltedGuid);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Optional<User> to = this.userService.getUser(userId);
        if (to.isEmpty()) {
            throw new RuntimeException("User with " + userId + " not found on this server.");
        }

        this.messageService.markAsSeen(me, to.get().getId(), messageId);
    }


}
