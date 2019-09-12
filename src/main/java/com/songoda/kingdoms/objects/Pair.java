package com.songoda.kingdoms.objects;

public class Pair<O1, O2> {

	private final O1 object1;
	private final O2 object2;

	/**
	 * Constructs an instance with the values present.
	 *
	 * @throws IllegalArgumentException if either value is null.
	 */
	protected Pair(O1 object1, O2 object2) {
		if (object1 == null || object2 == null)
			throw new IllegalArgumentException();
		this.object1 = object1;
		this.object2 = object2;
	}

	/**
	 * Returns a {@code DoubleObject} with the specified non-null values.
	 *
	 * @param <O1> The class of object value 1.
	 * @param <O@> The class of object value 2.
	 * @return a {@code DoubleObject} with the values.
	 */
	public static <O1, O2> Pair<O1, O2> of(O1 object1, O2 object2) {
		return new Pair<O1, O2>(object1, object2);
	}

	/**
	 * Grab the first element.
	 *
	 * @return the first non-null value held by this {@code DoubleObject}
	 */
	public O1 getFirst() {
		return object1;
	}

	/**
	 * Grab the second element.
	 *
	 * @return the second non-null value held by this {@code DoubleObject}
	 */
	public O2 getSecond() {
		return object2;
	}

	/**
	 * Indicates whether some other object is "equal to" this DoubleObject. The
	 * other object is considered equal if:
	 *
	 * @param object an object to be tested for equality
	 * @return {code true} if the other object is "equal to" this object
	 * otherwise {@code false}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;

		if (!(object instanceof Pair))
			return false;

		final Pair<?,?> other = (Pair<?,?>) object;
		if ((object1 == null) ? (other.object1 == null) : object1.equals(other.object1))
			return ((object2 == null) ? (other.object2 == null) : object2.equals(other.object2));
		return false;
	}

}
