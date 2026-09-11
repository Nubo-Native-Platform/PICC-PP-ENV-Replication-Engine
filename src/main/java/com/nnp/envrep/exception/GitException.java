/**
 * GitException.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
package com.nnp.envrep.exception;

/**
 * GitException.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
public class GitException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3903340898113198930L;

	public GitException(String s, Exception ex) {
		super(s, ex);
	}
	
	public GitException(String s) {
		super(s);
	}

}
