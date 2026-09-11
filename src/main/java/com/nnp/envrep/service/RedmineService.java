/**
 * RedmineService.java
 *
 * @author AC
 * @date 29-Apr-2025
 */
package com.nnp.envrep.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.nnp.envrep.event.GitOpsEvent;
import com.nnp.envrep.exception.EnvReplEngineEx;
import com.nnp.envrep.exception.RedmineException;
import com.nnp.envrep.model.EnvReqComponent;
import com.nnp.envrep.model.rm.Issue;
import com.nnp.envrep.model.rm.Issue__1;
import com.nnp.envrep.model.rm.ProjMembership;
import com.nnp.envrep.model.rm.Project;
import com.nnp.envrep.repo.EnvRepo;
import com.nnp.envrep.repo.EnvReqRepo;

import lombok.extern.slf4j.Slf4j;

/**
 * RedmineService.java
 *
 * @author AC
 * @date 29-Apr-2025
 */
@Service
@Slf4j
public class RedmineService {
	
	@Qualifier("webClientRedmineInt")
	@Autowired
	private WebClient webClientRedmine;
	
	@Value("${redmine.apiKey:}")
	private String redmineApiKey;
	
	@Value("${redmine.project.name:EIIMP Environment Support}")
	private String supportProjectName;
	
	@Autowired
	private EnvReqRepo envReqRepo;
	
	public void createSupportTicket(GitOpsEvent event) {
		
		try {
			//retrive projects and filter it based on the project name
			List<Project> rmProjects =  webClientRedmine.get().uri(uriBuilder -> uriBuilder.path("/api/getProjects").queryParam("apiKey", redmineApiKey).build()).retrieve().bodyToFlux(Project.class).collectList().block();
			
			Project project = rmProjects.stream().filter(p-> supportProjectName.equalsIgnoreCase(p.getName())).findFirst().orElseThrow();
			//retrive associated members of the project to whome task will be asssigned
			//List<ProjMembership> projectmemberships = webClientRedmine.get().uri("/api/getMembershipsForProj/{projId}", project.getId()).retrieve().toEntity(new ParameterizedTypeReference<List<ProjMembership>>() {}).block().getBody();
			
			//retrieve components associated with request. Name of the components will be present in ticket
			List<EnvReqComponent> envReqComponenets = envReqRepo.findById(event.getReqId()).get().getReqComp();
			String comonenetName = "";
			for(EnvReqComponent reqComp : envReqComponenets) {				
				if(comonenetName.isBlank())
					comonenetName = comonenetName + reqComp.getBbComponent().getCompName();
				else
					comonenetName = comonenetName + " \n "+reqComp.getBbComponent().getCompName();
			};
			
			//create issue at redmine
			Issue issue = new Issue();
			Issue__1 issue_1 = new Issue__1();
			issue_1.setProjectId(project.getId());
			issue_1.setTrackerId(6);
			issue_1.setSubject("Git ops run status check for request - "+event.getReqId());
			issue_1.setDescription("Following components has been submitted to git ops for deployement \n\n"+comonenetName);
			
			issue.setIssue(issue_1);
			
			ResponseEntity<String> stringResponseEntity = webClientRedmine.post().uri(uriBuilder -> uriBuilder.path("/api/createSupportIssue").queryParam("apiKey", redmineApiKey).build()).body(BodyInserters.fromValue(issue)).retrieve().toEntity(String.class).toFuture().get();
			
		} catch (Exception e) {
			log.error("Exception occurred while creating ticket");
			throw new RedmineException("Exception occurred while creating ticket for request id - "+event.getReqId(), e);
		}
	}

}
