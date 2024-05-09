package com.ignis.igrobotics.core.util;

import com.mojang.datafixers.util.Pair;

public class Tuple<A, B> {
	
	public A first;
	public B second;
	
	public Tuple(A first, B second) {
		this.first = first;
		this.second = second;
	}
	
	public A getFirst() {
		return first;
	}
	
	public B getSecond() {
		return second;
	}
	
	@Override
	public int hashCode() {
		return first.hashCode() | second.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Tuple)) return false;
		return first.equals(((Tuple<?, ?>) obj).getFirst()) && second.equals(((Tuple<?, ?>) obj).getSecond());
	}

	public static <A, B> Pair<A, B> toPair(Tuple<A, B> tuple) {
		return new Pair<>(tuple.first, tuple.second);
	}

}
