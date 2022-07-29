package ru.georgii.fonarserver.user;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.imageio.ImageIO;
import javax.persistence.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "users")
public class User {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @JsonIgnore
    byte[] photo;
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    @JsonIgnore
    private String uid;
    @Column(unique = true)
    private String nickname;
    private String firstname;
    private String lastname = null;
    private String bio = null;

    public User(String uid) {
        this.uid = uid;
    }

    protected User() {
    }

    public static void drawStringTopLeft(Graphics2D g, String s) {
        g.setFont(new Font("TimesRoman", Font.PLAIN, 128 * 3 / 4));

        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        // Determine the X coordinate for the text
        int x = (128 - metrics.stringWidth(s)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = ((128 - metrics.getHeight()) / 2) + metrics.getAscent();


        g.drawString(s, x, y);
    }

    public byte[] getPhotoByteArray() {

        if (this.photo == null) {
            BufferedImage bi = new BufferedImage(128, 128,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig2 = bi.createGraphics();

            ig2.setBackground(Color.WHITE);
            ig2.setColor(Color.BLACK);
            ig2.clearRect(0, 0, 128, 128);
            drawStringTopLeft(ig2, (firstname != null) && (firstname.length() > 0) ? "" + firstname.charAt(0) : "0");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(bi, "png", baos);
            } catch (IOException e) {
                return null;
            }

            return baos.toByteArray();

        }
        return this.photo;
    }

    public void setPhotoByteArray(byte[] photo) {
        this.photo = photo;
    }

    @JsonAnyGetter
    public Map<String, Object> avatarUrl() {
        Map<String, Object> m = new HashMap<>();
        m.put("avatarUrl", "/v1.0/user/photo?uid=" + id);
        return m;
    }

    public ObjectNode toObjectNode(ObjectMapper mapper) {
        ObjectNode o = mapper.createObjectNode();
        o.put("id", getId());
        o.put("firstname", getFirstname());
        o.put("lastname", getLastname());
        o.put("bio", getBio());
        o.put("nickname", getNickname());
        o.put("avatarUrl", "/v1.0/user/photo?uid=" + getId());
        o.put("avatarBytes", getPhotoByteArray());
        return o;
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

}
