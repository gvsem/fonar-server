package ru.georgii.fonarserver.dialog.dto;

import ru.georgii.fonarserver.dialog.Message;
import ru.georgii.fonarserver.user.User;

public class DialogDto {

    public Message lastMessage;

    public User user;

    public byte[] avatarBytes;

    public boolean lastMessageIsToMe;

    public Long unreadMessages;

}
