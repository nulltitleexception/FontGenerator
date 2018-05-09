package io.proffitt.FontGenerator;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Program {
    public static void main(String[] args) throws IOException {
        String name = "Courier New";
        int size = 72;
        int charnum = 256;
        ByteBuffer buf = ByteBuffer.allocate(4 * (((charnum + 1) * size * 2) + 2));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        BufferedImage img = genAtlas(name, size, charnum, buf);
        ImageIO.write(img, "PNG", new File(name + ".png"));
        DataOutputStream os = new DataOutputStream(new FileOutputStream(name + ".fdat"));
        os.write(buf.array());
        os.close();
    }

    static BufferedImage genAtlas(String font, int maxSize, int charNum, ByteBuffer data) {
        ArrayList<BufferedImage> imgs = new ArrayList<BufferedImage>();
        data.putInt(maxSize);
        data.putInt(charNum);
        int width = 0;
        int height = 0;
        for (int i = 1; i <= maxSize; i++) {
            ArrayList<Integer> pos = new ArrayList<Integer>();
            BufferedImage img = fillGlyphs(font, i, charNum, pos);
            imgs.add(img);
            width = width < img.getWidth() ? img.getWidth() : width;
            data.putInt(height);
            height += img.getHeight();
            data.putInt(height);
            for (int j = 0; j < pos.size(); j++) {
                data.putInt(pos.get(j));
            }
        }

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = ret.getGraphics();
        int h = 0;
        for (int i = 0; i < imgs.size(); i++) {
            g.drawImage(imgs.get(i), 0, h, null);
            h += imgs.get(i).getHeight();
        }
        return ret;

    }

    static BufferedImage fillGlyphs(String font, int size, int charNum, ArrayList<Integer> positions) {
        ArrayList<BufferedImage> imgs = new ArrayList<BufferedImage>();
        int height = 0;
        int width = 0;
        for (int i = 0; i < charNum; i++) {
            imgs.add(getText(font, size, "" + ((char) i)));
            if (imgs.get(i) != null) {
                height = height < imgs.get(i).getHeight() ? imgs.get(i).getHeight() : height;
                width += imgs.get(i).getWidth();
            }
        }
        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = ret.getGraphics();
        int pos = 0;
        for (int i = 0; i < charNum; i++) {
            if (imgs.get(i) == null) {
                positions.add(pos);
                positions.add(pos);
            } else {
                g.drawImage(imgs.get(i), pos, 0, null);
                positions.add(pos);
                pos += imgs.get(i).getWidth();
                positions.add(pos);
            }
        }
        return ret;
    }

    static BufferedImage getText(String font, int size, String text) {
        java.awt.Font awtFont = new java.awt.Font(font, java.awt.Font.PLAIN, size);
        BufferedImage empty = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = empty.createGraphics();
        g.setFont(awtFont);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(text);
        int h = fm.getHeight();
        if (w == 0) {
            return null;
        }
        BufferedImage ret = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        g.dispose();
        g = ret.createGraphics();
        g.setFont(awtFont);
        g.setPaint(Color.white);
        fm = g.getFontMetrics();
        LineMetrics lm = fm.getLineMetrics(text, g);
        g.drawString(text, 0, lm.getAscent());
        g.dispose();
        return ret;
    }
}
