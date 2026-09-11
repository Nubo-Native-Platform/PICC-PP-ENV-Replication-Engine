package com.nnp.envrep.exception;

public class HAProxyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public HAProxyException(String s, Exception ex) {
		super(s, ex);
	}
	
	public HAProxyException(String s) {
		super(s);
	}

}
