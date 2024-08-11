package com.ignis.igrobotics.common.helpers.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

public class MathUtil {
	
	/**
	 * Downsizes a rectangle by amount pixel in every direction
	 * @param rect the rectangle to downsize
	 * @param amount the amount of pixels to downsize
	 * @return the downsized rectangle
	 */
	public static Rectangle downsizeRect(Rectangle rect, int amount) {
		return new Rectangle(rect.x + amount, rect.y + amount, rect.width - amount * 2, rect.height - amount * 2);
	}
	
	public static int standardSearch(int min, int max, Predicate<Integer> condition) {
		Integer[] array = new Integer[max - min];
		Arrays.setAll(array, i -> min + i);
		return standardSearch(array, condition);
	}
	
	public static <T> int standardSearch(T[] array, Predicate<T> condition) {
		for(int i = array.length - 1; i >= 0; i--) {
			if(condition.test(array[i])) {
				return i;
			}
		}
		return array.length - 1;
	}
	
	public static <T> Collection<T> subset(Collection<T> collection, Predicate<T> cond) {
		ArrayList<T> subset = new ArrayList<>();
		for(T t : collection) {
			if(cond.test(t)) {
				subset.add(t);
			}
		}
		return subset;
	}
	
	public static int restrict(int min, double value, int max) {
		return Math.max(min, Math.min((int) value, max));
	}

	public static float asymptote(int x, float min, float max) {
		return (float) ((-((max - min) * Math.exp(-x) - (max - min)/2)) + (max - min)/2);
	}

}
