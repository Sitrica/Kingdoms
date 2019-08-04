package me.limeglass.kingdoms.objects;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DoubleOptional<O1, O2> {

	private final O1 object1;
	private final O2 object2;

	/**
	 * Constructs an absent instance.
	 */
	private DoubleOptional() {
		this.object1 = null;
		this.object2 = null;
	}

	/**
	 * Constructs an instance with the values present.
	 *
	 * @param o1 Object 1 that is non null.
	 * @throws IllegalArgumentException if values are null.
	 */
	private DoubleOptional(O1 object1, O2 object2) {
		this.object1 = object1;
		this.object2 = object2;
	}

	/**
	 * Returns a {@code DoubleOptional} with the specified present non-null values.
	 *
	 * @param <O1> The class of object value 1.
	 * @param <O@> The class of object value 2.
	 * @return a {@code DoubleOptional} with the values present.
	 */
	public static <O1, O2> DoubleOptional<O1, O2> of(O1 object1, O2 object2) {
		return new DoubleOptional<O1, O2>(object1, object2);
	}

	/**
	 * Return {@code true} if the first object is present, otherwise {@code false}.
	 *
	 * @return {@code true} if there is a value present, otherwise {@code false}
	 */
	public boolean isFirstPresent() {
		return object1 != null;
	}

	/**
	 * If the first value is present in this {@code DoubleOptional}, returns the value,
	 * otherwise throws {@code IllegalStateException}.
	 *
	 * @return the non-null value held by this {@code DoubleOptional}
	 * @throws IllegalStateException if there is no value present
	 */
	public O1 getFirst() {
		if (isFirstPresent())
			return object1;
		throw new IllegalStateException("First object is absent.");
	}

	/**
	 * Return {@code true} if the second object is present, otherwise {@code false}.
	 *
	 * @return {@code true} if there is a value present, otherwise {@code false}
	 */
	public boolean isSecondPresent() {
		return object2 != null;
	}

	/**
	 * If the second value is present in this {@code DoubleOptional}, returns the value,
	 * otherwise throws {@code IllegalStateException}.
	 *
	 * @return the non-null value held by this {@code DoubleOptional}
	 * @throws IllegalStateException if there is no value present
	 */
	public O2 getSecond() {
		if (isFirstPresent())
			return object2;
		throw new IllegalStateException("Second object is absent.");
	}

	/**
	 * Return the first value if present, otherwise return {@code other}.
	 *
	 * @param other the value to be returned if there is no first value present, may not
	 * be null
	 * @return the first value, if present, otherwise {@code other}
	 * @throws IllegalArgumentException if {@code other} is null
	 */
	public O1 orFirst(O1 other) {
		if (other == null)
			throw new IllegalArgumentException("null may not be passed as an argument in orFirst; use orNull() instead.");
		return isFirstPresent() ? object1 : other;
	}

	/**
	 * Return the second value if present, otherwise return {@code other}.
	 *
	 * @param other the value to be returned if there is no second value present, may not
	 * be null
	 * @return the second value, if present, otherwise {@code other}
	 * @throws IllegalArgumentException if {@code other} is null
	 */
	public O2 orSecond(O2 other) {
		if (other == null)
			throw new IllegalArgumentException("null may not be passed as an argument in orSecond; use orNull() instead.");
		return isSecondPresent() ? object2 : other;
	}

	/**
	 * Return the first value if present, otherwise return {@code other}.
	 *
	 * @param otherSupplier a {@code Supplier} whose result is returned if no value
	 * is present
	 * @return the first value, if present, otherwise {@code other}
	 * @throws IllegalArgumentException if {@code other} is null
	 */
	public O1 orFirst(Supplier<O1> otherSupplier) {
		if (otherSupplier == null)
			throw new IllegalArgumentException("null may not be passed as an argument in orFirst; use orNull() instead.");
		return isFirstPresent() ? object1 : otherSupplier.get();
	}

	/**
	 * Return the second value if present, otherwise return {@code other}.
	 *
	 * @param otherSupplier a {@code Supplier} whose result is returned if no value
	 * is present
	 * @return the second value, if present, otherwise {@code other}
	 * @throws IllegalArgumentException if {@code other} is null
	 */
	public O2 orSecond(Supplier<O2> otherSupplier) {
		if (otherSupplier == null)
			throw new IllegalArgumentException("null may not be passed as an argument in orSecond; use orNull() instead.");
		return isSecondPresent() ? object2 : otherSupplier.get();
	}

	/**
	 * Return the contained first value, if present, otherwise throw an exception
	 * instance provided as the parameter.
	 *
	 * @param <X> Type of the exception to be thrown
	 * @param throwable The throwable instance to be thrown
	 * @return the present first value
	 * @throws X if there is no value present
	 * @throws IllegalArgumentException if {@code throwable} is null
	 */
	public <X extends Throwable> O1 orFirstThrow(X throwable) throws X {
		if (throwable == null)
			throw new IllegalArgumentException("null may not be passed as an argument in orFirstThrow; use orNull() instead.");
		if (isFirstPresent())
			return object1;
		throw throwable;
	}

	/**
	 * Return the contained second value, if present, otherwise throw an exception
	 * instance provided as the parameter.
	 *
	 * @param <X> Type of the exception to be thrown
	 * @param throwable The throwable instance to be thrown
	 * @return the present second value
	 * @throws X if there is no value present
	 * @throws IllegalArgumentException if {@code throwable} is null
	 */
	public <X extends Throwable> O2 orSecondThrow(X throwable) throws X {
		if (throwable == null)
			throw new IllegalArgumentException("null may not be passed as an argument in orSecondThrow; use orNull() instead.");
		if (isSecondPresent())
			return object2;
		throw throwable;
	}

	/**
	 * Return the first contained value, if present, otherwise null.
	 *
	 * @return the present value, if present, otherwise null
	 */
	public O1 orFirstNull() {
		return isFirstPresent() ? object1 : null;
	}

	/**
	 * Return the first contained value, if present, otherwise null.
	 *
	 * @return the present value, if present, otherwise null
	 */
	public O2 orSecondNull() {
		return isSecondPresent() ? object2 : null;
	}

	/**
	 * If the first value is present, invoke the specified consumer with the value,
	 * otherwise do nothing.
	 *
	 * @param consumer block to be executed if a value is present
	 */
	public void ifFirstPresent(Consumer<O1> consumer) {
		if (isFirstPresent()) {
			consumer.accept(object1);
		}
	}

	/**
	 * If the second value is present, invoke the specified consumer with the value,
	 * otherwise do nothing.
	 *
	 * @param consumer block to be executed if a value is present
	 */
	public void ifSecondPresent(Consumer<O2> consumer) {
		if (isSecondPresent()) {
			consumer.accept(object2);
		}
	}

	/**
	 * If the first value is present, invoke the specified consumer with the value,
	 * otherwise invoke the function passed as the second parameter.
	 *
	 * @param consumer block to be executed if the first value is present
	 * @param runnable block to be executed if the first value is absent
	 */
	public void ifFirstPresentOrElse(Consumer<O1> consumer, Runnable runnable) {
		if (isFirstPresent()) {
			consumer.accept(object1);
		} else {
			runnable.run();
		}
	}

	/**
	 * If the second value is present, invoke the specified consumer with the value,
	 * otherwise invoke the function passed as the second parameter.
	 *
	 * @param consumer block to be executed if the second value is present
	 * @param runnable block to be executed if the second value is absent
	 */
	public void ifSecondPresentOrElse(Consumer<O2> consumer, Runnable runnable) {
		if (isSecondPresent()) {
			consumer.accept(object2);
		} else {
			runnable.run();
		}
	}

	/**
	 * Indicates whether some other object is "equal to" this DoubleOptional. The
	 * other object is considered equal if:
	 * <ul>
	 * <li>it is also an {@code DoubleOptional} and;
	 * <li>both instances have no values present or;
	 * <li>the present values are "equal to" each other via {@code equals()}.
	 * </ul>
	 *
	 * @param object an object to be tested for equality
	 * @return {code true} if the other object is "equal to" this object
	 * otherwise {@code false}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;

		if (!(object instanceof DoubleOptional))
			return false;

		final DoubleOptional<?,?> other = (DoubleOptional<?,?>) object;
		if ((object1 == null) ? (other.object1 == null) : object1.equals(other.object1))
			return ((object2 == null) ? (other.object2 == null) : object2.equals(other.object2));
		return false;
	}

}
