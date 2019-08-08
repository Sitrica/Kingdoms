package com.songoda.kingdoms.placeholders;

public abstract class SimplePlaceholder extends Placeholder<String> {
	
	public SimplePlaceholder(String... syntax) {
		super(syntax);
	}

	@Override
	public final String replace(String object) {
		for (String syntax : syntaxes)
			if (object.contains(syntax))
				return get();
		return null;
	}
	
	public abstract String get();
	
}
