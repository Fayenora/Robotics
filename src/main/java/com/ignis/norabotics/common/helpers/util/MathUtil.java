package com.ignis.norabotics.common.helpers.util;

import au.edu.federation.utils.Vec3f;
import com.ignis.norabotics.common.helpers.types.Tuple;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

	public static Tuple<Float, Integer> argmin(float... args) {
		float currentMin = Float.MAX_VALUE;
		int currentInd = args.length + 1;
		for(int i = 0; i < args.length; i++) {
			if(args[i] < currentMin) {
				currentMin = args[i];
				currentInd = i;
			}
		}
		return new Tuple<>(currentMin, currentInd);
	}

	public static double circularRange(double val, double min, double max) {
		return (val - min) % (max - min) + min;
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
		List<T> subset = new ArrayList<>();
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

	public static float clamp(float min, float value, float max) {
		return Math.max(min, Math.min(value, max));
	}

	public static float asymptote(int x, float min, float max) {
		return (float) ((-((max - min) * Math.exp(-x) - (max - min)/2)) + (max - min)/2);
	}

	public static Vec3f of(Vec3 vec) {
		return new Vec3f((float) vec.x, (float) vec.y, (float) vec.z);
	}

}
