package com.github.ratelimiter4c.exception;

public class AsmException extends RuntimeException {
	private static final long serialVersionUID = 2L;

    private String msg;

    public AsmException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public AsmException(Throwable e) {
		super(e);
	}

	public AsmException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	@Override
	public String getMessage() {
		return this.msg;
	}
}
