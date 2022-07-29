package ru.georgii.fonarserver.dialog;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.georgii.fonarserver.user.User;

import javax.persistence.*;
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

    public Long getId() {
        return id;
    }

    public Long getFromUserId() {
        return fromUser.getId();
    }

    public Long getToUserId() {
        return toUser.getId();
    }

    public Date getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public boolean isSeen() {
        return seen;
    }

}
