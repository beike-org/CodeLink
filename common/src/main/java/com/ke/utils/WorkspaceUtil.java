package com.ke.utils;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * 专门用在/workspace里面的工具类
 */
public class WorkspaceUtil {
	/**
	 * 将usageInfo里面获取的icon转换为base64
	 */
	public static String convertIconToBase64(Icon icon) {
		Image image = getImageFromIcon(icon);
		if (image == null) {
			return null;
		}

		BufferedImage bufferedImage = toBufferedImage(image);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			ImageIO.write(bufferedImage, "png", outputStream);
			byte[] imageBytes = outputStream.toByteArray();
			return Base64.getEncoder().encodeToString(imageBytes);
		} catch (IOException e) {
			return null;
		}
	}

	private static Image getImageFromIcon(Icon icon) {
		if (icon instanceof ImageIcon) {
			return ((ImageIcon) icon).getImage();
		} else {
			BufferedImage bufferedImage = new BufferedImage(
					icon.getIconWidth(),
					icon.getIconHeight(),
					BufferedImage.TYPE_INT_ARGB
			);
			Graphics g = bufferedImage.getGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
			return bufferedImage;
		}
	}

	private static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		BufferedImage bufferedImage = new BufferedImage(
				img.getWidth(null),
				img.getHeight(null),
				BufferedImage.TYPE_INT_ARGB
		);
		Graphics2D bGr = bufferedImage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		return bufferedImage;
	}


}
