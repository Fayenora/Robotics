package com.ignis.igrobotics.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.Font;

public class StringUtil {
	
	public static String replaceAll(String original, Set<String> matchesAny, String replacement) {
		String toReturn = original;
		for(String match : matchesAny) {
			toReturn = toReturn.replace(match, replacement);
		}
		return toReturn;
	}
	
	public static List<String> calculateStringSplit(Font font, String string, int maxWidth) {
		ArrayList<String> parts = new ArrayList<String>();
		String remainder = string;
		while(font.width(remainder) > maxWidth) {
			//How long can the string be while still fitting in this row?
			remainder = remainder.trim();
			final String s = remainder;
			int lengthOfRow = MathUtil.standartSearch(1, remainder.length(), l -> font.width(s.substring(0, l)) <= maxWidth);
			parts.add(remainder.substring(0, lengthOfRow));
			remainder = remainder.substring(lengthOfRow);
		}
		parts.add(remainder.trim());
		return parts;
	}

}
