package com.songoda.kingdoms.placeholders;

public class SimplePlaceholder extends Placeholder<String> {
	
	public SimplePlaceholder(String... syntaxes) {
		super(syntaxes);
	}

	@Override
	public String replace(String string) {
		return string;
	}
	
}
