/**
 * RedmineException.java
 *
 * @author AC
 * @date 30-Apr-2025
 */
package com.nnp.envrep.exception;

/**
 * RedmineException.java
 *
 * @author AC
 * @date 30-Apr-2025
 */
public class RedmineException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8420687606280699285L;
	
	public RedmineException(String s, Exception ex) {
		super(s, ex);
	}
	
	public RedmineException(String s) {
		super(s);
	}

}
