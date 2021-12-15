package com.github.ratelimiter4c.exception;

public class DBException extends RuntimeException {

    private String msg;

    public DBException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public DBException(Throwable e) {
		super(e);
	}

	public DBException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	@Override
	public String getMessage() {
		return this.msg;
	}
}
