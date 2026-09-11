/**
 * GitopsEventListener.java
 *
 * @author AC
 * @date 28-Apr-2025
 */
package com.nnp.envrep.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.nnp.envrep.model.EnvReqCommunication;
import com.nnp.envrep.repo.EnvReqCommunicationRepo;
import com.nnp.envrep.service.HAProxyService;
import com.nnp.envrep.service.RedmineService;

import lombok.extern.slf4j.Slf4j;

/**
 * GitopsEventListener.java
 *
 * @author AC
 * @date 28-Apr-2025
 */
@Component
@Slf4j
public class GitopsEventListener {
	
	@Autowired
	private EnvReqCommunicationRepo communicationRepo;
	
	@Autowired
	private RedmineService redmineService;
	
	@Autowired
	private HAProxyService proxyService;
	
	@Async
	@EventListener
	public void handleEvents(GitOpsEvent event) {

//		proxyService.registerCompHaproxy(event);
	}
}
