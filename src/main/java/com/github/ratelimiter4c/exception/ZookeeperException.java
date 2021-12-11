package com.github.ratelimiter4c.exception;

public class ZookeeperException extends RuntimeException {

    private String msg;

    public ZookeeperException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public ZookeeperException(Throwable e) {
		super(e);
	}

	public ZookeeperException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	@Override
	public String getMessage() {
		return this.msg;
	}
}
