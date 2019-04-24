package com.songoda.kingdoms.placeholders;

import com.google.common.reflect.TypeToken;

public abstract class Placeholder<T> {
	
	private String[] syntaxes;
	
	public Placeholder(String... syntaxes) {
		this.syntaxes = syntaxes;
	}
	
	public String[] getSyntaxes() {
		return syntaxes;
	}
	
	@SuppressWarnings("serial")
	public Class<? super T> getType() {
		return new TypeToken<T>(getClass()){}.getRawType();
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
		Object replacement = replace((T) object);
		if (replacement == null)
			return null;
		return replacement.toString();
	}
	
}
