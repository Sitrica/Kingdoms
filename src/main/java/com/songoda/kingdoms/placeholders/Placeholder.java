package com.songoda.kingdoms.placeholders;

import java.lang.reflect.ParameterizedType;

public abstract class Placeholder<T> {
	
	private String[] syntaxes;
	
	public Placeholder(String... syntaxes) {
		this.syntaxes = syntaxes;
	}
	
	public String[] getSyntaxes() {
		return syntaxes;
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	/**
	 * Replace a placeholder from the given object.
	 * 
	 * @param object The object to get the placeholder replacement from.
	 * @return The final replaced placeholder.
	 */
	public abstract Object replace(T object);
	
	@SuppressWarnings("unchecked")
	public String replace_i(Object object) {
		return replace((T) object).toString();
	}
	
}
