package com.io.norabotics.common.helpers.util;

import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StringUtil {
	
	public static String replaceAll(String original, Set<String> matchesAny, String replacement) {
		String toReturn = original;
		for(String match : matchesAny) {
			toReturn = toReturn.replace(match, replacement);
		}
		return toReturn;
	}

	@OnlyIn(Dist.CLIENT)
	public static List<String> calculateStringSplit(Font font, String string, int maxWidth) {
		List<String> parts = new ArrayList<>();
		String remainder = string;
		while(font.width(remainder) > maxWidth) {
			//How long can the string be while still fitting in this row?
			remainder = remainder.trim();
			final String s = remainder;
			int lengthOfRow = MathUtil.standardSearch(1, remainder.length(), l -> font.width(s.substring(0, l)) <= maxWidth);
			parts.add(remainder.substring(0, lengthOfRow));
			remainder = remainder.substring(lengthOfRow);
		}
		parts.add(remainder.trim());
		return parts;
	}

	public static String enumToString(Enum<?>[] values) {
		StringBuilder viablePermissions = new StringBuilder();
		for(Enum<?> en : values) {
			viablePermissions.append(", \"").append(en.name()).append("\"");
		}
		viablePermissions.delete(0, 2);
		return viablePermissions.toString();
	}

	public static String titleCase(String s) {
		s = s.replaceAll("_", " ");
		StringBuilder builder = new StringBuilder();
		char prevCharacter = ' ';
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			builder.append(prevCharacter == ' ' ? Character.toUpperCase(c) : c);
			prevCharacter = c;
		}
		return builder.toString();
	}

	/**
	 * Generate a string indicating a time <1 hour
	 * @param time in milliseconds
	 * @return A string representing a time stamp, f.e. "4m 23s"
	 */
	public static String getTimeDisplay(int time) {
		if(time >= 1200) {
			return (time / 1200) + "m " + ((time % 1200) / 20) + "s";
		}
		return (time / 20) + " s";
	}

	public static String getEnergyDisplay(long rf) {
		if(rf >= 1e12) {
			return (rf / 1e12) + "TRF";
		}
		if(rf >= 1e9) {
			return (rf / 1e9) + "GRF";
		}
		if(rf >= 1e6) {
			return (rf / 1e6) + "MRF";
		}
		if(rf >= 1e3) {
			return (rf / 1e3) + "kRF";
		}
		return rf + "RF";
	}

	public static String prettyPrintLargeNumber(Number number, DecimalFormat format) {
		if(number.longValue() >= 1e12) {
			return format.format(number.longValue() / 1e12) + "T";
		}
		if(number.longValue() >= 1e9) {
			return format.format(number.longValue() / 1e9) + "B";
		}
		if(number.intValue() >= 1e6) {
			return format.format(number.intValue() / 1e6) + "M";
		}
		if(number.intValue() >= 1e3) {
			return format.format(number.intValue() / 1e3) + "K";
		}
		return format.format(number.longValue());
	}

}
