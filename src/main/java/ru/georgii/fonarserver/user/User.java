package ru.georgii.fonarserver.user;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

import javax.imageio.ImageIO;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "users")
public class User implements Principal {

    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    @JsonIgnore
    private String uid;

    @NotNull
    @Length(min = 1, max = 30)
    private String firstname;

    @Length(min = 0, max = 30)
    private String lastname = null;

    @Length(min = 0, max = 255)
    private String bio = null;

    @NotNull
    @Length(min = 1, max = 30)
    @Column(unique = true)
    private String nickname;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    byte[] photo;

    public User(String uid) {
        this.uid = uid;
    }

    protected User() {
    }

    @Schema(example = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAAEElEQVR4nGJ6VrQAEAAA//8EQgH7dTCZ8gAAAABJRU5ErkJggg==",
            description = "Base64-encoded avatar image thumbnail", required = true)
    public byte[] getAvatarBytes() {
        if (this.photo == null) {
            return UserAvatarHelper.drawLetterAvatar(firstname);
        }
        return this.photo;
    }

    public void setAvatarBytes(byte[] photo) {
        this.photo = photo;
    }


    @Schema(example = "1", description = "Id of user", required = true)
    public Long getId() {
        return id;
    }

    @Schema(example = "gumilev", description = "Unique nickname on this server", required = true)
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Schema(example = "Nikolai", description = "Firstname", required = true)
    public String getFirstname() {
        return firstname;
    }

    @Schema(example = "/v1.0/user/photo?uid=1", description = "Relative link to user's avatar image", required = true)
    public String getAvatarUrl() {
        return "/v1.0/user/photo?uid=" + getId();
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @Schema(example = "/v1.0/user/photo?uid=1", description = "Lastname")
    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    @Schema(example = "Talented poet from Petrograd", description = "Short phrase about user")
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return "" + id;
    }
}
