package ru.georgii.fonarserver.dialog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.georgii.fonarserver.dialog.dto.DialogDto;
import ru.georgii.fonarserver.gateway.MessageGateway;
import ru.georgii.fonarserver.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class MessageService {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageGateway messageGateway;

//    public Dialog getDialogBetween(User a, User b) {
//        if ((a == null) || (b == null)) {
//            throw new RuntimeException("At least one of users is null.");
//        }
//
//        List<Dialog> dialogs = this.dialogRepository.findDialogByMembersMatches(Arrays.asList(a, b));
//        if (dialogs.size() == 0) {
//            Dialog d = new Dialog(a, b);
//            return this.dialogRepository.save(d);
//        }
//        return dialogs.get(0);
//    }
//
//    public void postMessage(Dialog dialog, Message m) {
//        if ((dialog == null) || (m == null)) {
//            throw new RuntimeException("Dialog and message must not be null.");
//        }
//
//        m.dialog = dialog;
//        this.messageRepository.save(m);
//    }

    public Message sendMessage(User from, User to, Message m) {
        m.fromUser = from;
        m.toUser = to;
        m.date = new Date();
        m.seen = false;
        Message r = this.messageRepository.save(m);
        messageGateway.notifyAboutNewMessage(r);
        return r;
    }

    public void markAsSeen(User from, Long toId, Long messageId) {
        Message m = this.messageRepository.findMessageInConversation(from.getId(), toId, messageId);
        if (m == null) {
            return;
        }
        m.seen = true;
        this.messageRepository.save(m);
        this.messageGateway.notifyAboutSeen(m);
    }

    public List<DialogDto> getDialogs(User from, Long quantity, Long offset) {
        List<Message> lastMessages = this.messageRepository.getDialogs(from, quantity, offset);
        List<DialogDto> dialogs = new ArrayList<>();
        for (Message m : lastMessages) {
            DialogDto d = new DialogDto();
            d.lastMessage = m;
            d.lastMessageIsToMe = !Objects.equals(from.getId(), m.fromUser.getId());
            d.user = d.lastMessageIsToMe ? m.fromUser : m.toUser;
            d.avatarBytes = d.user.getPhotoByteArray();
            d.unreadMessages = this.messageRepository.countMessagesBySeenIsFalseAndFromUserAndToUser
                    (d.lastMessageIsToMe ? m.fromUser : m.toUser, from);
            dialogs.add(d);
        }
        return dialogs;
    }

    public List<Message> getDialog(User a, User b, Long quantity, Long offset) {
        return this.messageRepository.getDialog(a, b, quantity, offset);
    }

}
