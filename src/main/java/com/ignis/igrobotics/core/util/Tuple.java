package com.ignis.igrobotics.core.util;

public class Tuple<A, B> {
	
	private A first;
	private B second;
	
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
		return first.equals(((Tuple)obj).getFirst()) && second.equals(((Tuple)obj).getSecond());
	}

}
