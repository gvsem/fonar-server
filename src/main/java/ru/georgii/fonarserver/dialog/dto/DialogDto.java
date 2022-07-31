package ru.georgii.fonarserver.dialog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.georgii.fonarserver.dialog.Message;
import ru.georgii.fonarserver.user.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class DialogDto {

    @Schema(description = "Last message in current conversation")
    public Message lastMessage;

    @Schema(description = "User which this conversation is associated with")
    public User user;

    @NotNull
    @Schema(example = "true", description = "Indicates that last message was sent to current user.")
    public boolean lastMessageIsToMe;

    @NotNull
    @Schema(example = "true", description = "Indicates that last message was sent to current user.")
    public Long unreadMessages;

}
