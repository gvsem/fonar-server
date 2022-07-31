package ru.georgii.fonarserver.dialog;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.georgii.fonarserver.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@JsonSerialize
public class Message {

    @ManyToOne
    protected User fromUser;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    protected User toUser;
    @Column(name = "creationDate")
    protected Date date;
    protected boolean seen = false;
    private @Id
    @GeneratedValue Long id;
    @Column(name = "messageText")
    private String text;

    public Message(String text) {
        this.text = text;
    }

    protected Message() {

    }

    @Schema(example = "90", description = "Id of message within given conversation (see toUserId)", required = true)
    public Long getId() {
        return id;
    }

    @Schema(example = "1", description = "Id of user from message was sent", required = true)
    public Long getFromUserId() {
        return fromUser.getId();
    }

    @Schema(example = "2", description = "Id of conversation (user) to which message was sent", required = true)
    public Long getToUserId() {
        return toUser.getId();
    }

    @Schema(example = "2001-01-01T03:00:00.000Z", description = "Date message has been sent", required = true)
    public Date getDate() {
        return date;
    }

    @Schema(example = "Hello, world!", description = "Text of message", required = true)
    public String getText() {
        return text;
    }

    @Schema(example = "false", description = "Flag indicates that message is seen", required = true)
    public boolean isSeen() {
        return seen;
    }

}
