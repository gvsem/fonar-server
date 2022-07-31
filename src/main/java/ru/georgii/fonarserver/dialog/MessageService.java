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

    public Message sendMessage(User from, User to, Message m) {
        m.fromUser = from;
        m.toUser = to;
        m.date = new Date();
        m.seen = false;
        Message r = this.messageRepository.save(m);
        messageGateway.notifyAboutNewMessage(r);
        return r;
    }

    public enum SEEN_METHOD_STATUS {
        OK,
        NO_MESSAGE_FOUND,
        ALREADY_SEEN
    }
    public SEEN_METHOD_STATUS markAsSeen(User from, Long toId, Long messageId) {
        Message m = this.messageRepository.findMessageInConversation(from.getId(), toId, messageId);
        if (m == null) {
            return SEEN_METHOD_STATUS.NO_MESSAGE_FOUND;
        }
        if (m.seen) {
            return SEEN_METHOD_STATUS.ALREADY_SEEN;
        }
        m.seen = true;
        this.messageRepository.save(m);
        this.messageGateway.notifyAboutSeen(m);
        return SEEN_METHOD_STATUS.OK;
    }

    public List<DialogDto> getDialogs(User from, Long quantity, Long offset) {
        List<Message> lastMessages = this.messageRepository.getDialogs(from, quantity, offset);
        List<DialogDto> dialogs = new ArrayList<>();
        for (Message m : lastMessages) {
            DialogDto d = new DialogDto();
            d.lastMessage = m;
            d.lastMessageIsToMe = !Objects.equals(from.getId(), m.fromUser.getId());
            d.user = d.lastMessageIsToMe ? m.fromUser : m.toUser;
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
