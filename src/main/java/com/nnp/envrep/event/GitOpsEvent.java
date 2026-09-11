/**
 * GitOpsEvent.java
 *
 * @author AC
 * @date 28-Apr-2025
 */
package com.nnp.envrep.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * GitOpsEvent.java
 *
 * @author AC
 * @date 28-Apr-2025
 */
@Getter
@Setter
@ToString
public class GitOpsEvent {
	
	private String eventMsg;
	private String reqId;

	/**
	 * @param eventMsg
	 */
	public GitOpsEvent(String reqId, String eventMsg) {
		super();
		this.eventMsg = eventMsg;
		this.reqId = reqId;
	}
}
