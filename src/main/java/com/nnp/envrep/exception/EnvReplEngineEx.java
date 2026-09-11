/**
 * EnvReplicationEnggEx.java
 *
 * @author AC
 * @date 28-Apr-2025
 */
package com.nnp.envrep.exception;

/**
 * EnvReplicationEnggEx.java
 *
 * @author AC
 * @date 28-Apr-2025
 */
public class EnvReplEngineEx extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1904302952670937381L;
	public EnvReplEngineEx(String s, Exception ex) {
		super(s, ex);
	}
	
	public EnvReplEngineEx(String s) {
		super(s);
	}
	

}

