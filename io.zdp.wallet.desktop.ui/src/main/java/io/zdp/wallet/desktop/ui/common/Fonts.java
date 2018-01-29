package io.zdp.wallet.desktop.ui.common;

import java.awt.Font;

public class Fonts {

	public static Font getMainFont() throws Exception {
		Font font = Font.createFont(Font.PLAIN, Fonts.class.getResourceAsStream("/fonts/DejaVuSerif.ttf"));
		font = font.deriveFont(16.0f);
		return font;
	}

}
