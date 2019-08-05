package com.songoda.kingdoms.objects.exceptions;

public class KingdomsAPIException extends RuntimeException {

	private static final long serialVersionUID = 990046857775247879L;
	private String message;

	public KingdomsAPIException(String message) {
		super(message);
		this.message = message;
	}

	public KingdomsAPIException(Throwable cause) {
		super(cause);
	}

	public KingdomsAPIException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}

	/**
	 * @return The error message.
	 */
	public String getErrorMessage() {
		return message;
	}

}
