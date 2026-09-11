/**
 * ArgoException.java
 *
 * @author AC
 * @date 06-May-2025
 */
package com.nnp.envrep.exception;

/**
 * ArgoException.java
 *
 * @author AC
 * @date 06-May-2025
 */
public class ArgoException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ArgoException(String s, Exception ex) {
		super(s, ex);
	}
	
	public ArgoException(String s) {
		super(s);
	}


}
