package com.ignis.igrobotics.core.util;

import java.awt.Rectangle;
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
	
	/**
	 * Find the rightmost index of an array that fulfills the condition
	 * @param <T> Any comparable data type
	 * @param array a sorted list, the search space
	 * @param condition what the returned element must fulfill
	 * @return the maximum element that fulfills the condition
	 */
	//FIXME
	public static <T extends Comparable<T>> int binarySearch(T[] array, Predicate<T> condition) {
		int l = 0;
		int r = array.length;
		while(l < r) {
			int m = Math.floorDiv(l + r, 2);
			if(!condition.test(array[m])) {
				r = m;
			} else {
				l = m - 1;
			}
		}
		return r - 1;
	}
	
	/**
	 * Find the rightmost index of an object that fulfills the condition
	 * @param min inclusive
	 * @param max exclusive
	 * @param condition what the returned element must fulfill
	 * @return the maximum element that fulfills the condition
	 */
	public static int binarySearch(int min, int max, Predicate<Integer> condition) {
		Integer[] array = new Integer[max - min];
		Arrays.setAll(array, i -> min + i);
		return binarySearch(array, condition);
	}
	
	public static int standartSearch(int min, int max, Predicate<Integer> condition) {
		Integer[] array = new Integer[max - min];
		Arrays.setAll(array, i -> min + i);
		return standartSearch(array, condition);
	}
	
	public static <T> int standartSearch(T[] array, Predicate<T> condition) {
		for(int i = array.length - 1; i >= 0; i--) {
			if(condition.test(array[i])) {
				return i;
			}
		}
		return array.length - 1;
	}
	
	public static <T> Collection<T> subset(Collection<T> collection, Predicate<T> cond) {
		ArrayList<T> subset = new ArrayList<T>();
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

}
