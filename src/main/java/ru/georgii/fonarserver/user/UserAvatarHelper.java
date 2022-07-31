package ru.georgii.fonarserver.user;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UserAvatarHelper {

    private static void drawStringTopLeft(Graphics2D g, String s) {
        g.setFont(new Font("TimesRoman", Font.PLAIN, 128 * 3 / 4));

        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        // Determine the X coordinate for the text
        int x = (128 - metrics.stringWidth(s)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = ((128 - metrics.getHeight()) / 2) + metrics.getAscent();


        g.drawString(s, x, y);
    }

    public static byte[] drawLetterAvatar(String text) {
        BufferedImage bi = new BufferedImage(128, 128,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = bi.createGraphics();

        ig2.setBackground(Color.WHITE);
        ig2.setColor(Color.BLACK);
        ig2.clearRect(0, 0, 128, 128);
        drawStringTopLeft(ig2, (text != null) && (text.length() > 0) ? "" + text.charAt(0) : "0");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", baos);
        } catch (IOException e) {
            return null;
        }

        return baos.toByteArray();
    }
}
