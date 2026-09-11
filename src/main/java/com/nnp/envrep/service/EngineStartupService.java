/**
 * EngineStartupService.java
 *
 * @author AC
 * @date 21-Apr-2025
 */
package com.nnp.envrep.service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nnp.envrep.config.GitOpsProperties;
import com.nnp.envrep.config.JMSConfig;
import com.nnp.envrep.event.GitOpsEvent;
import com.nnp.envrep.exception.ArgoException;
import com.nnp.envrep.exception.EnvReplEngineEx;
import com.nnp.envrep.exception.GitException;
import com.nnp.envrep.model.EnvReqComponent;
import com.nnp.envrep.model.EnvRequest;
import com.nnp.envrep.model.Environment;
import com.nnp.envrep.model.HostPlan;
import com.nnp.envrep.repo.EnvRepo;
import com.nnp.envrep.repo.EnvReqRepo;
import com.nnp.envrep.repo.HostPlanRepo;
import com.nnp.envrep.util.CustomFileUtil;
import com.nnp.envrep.util.GitlabActionUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * EngineStartupService.java
 *
 * @author AC
 * @date 21-Apr-2025
 */
@Service
@Slf4j
public class EngineStartupService {
	@Autowired
	private GitOpsProperties location;
	
	@Autowired
	private EnvRepo envRepo;
	
	@Autowired
	private EnvReqRepo envReqRepo;	
	
	@Autowired
	private HostPlanRepo hostPlanRepo;
	
	@Autowired
	private ApplicationEventPublisher appEventPub;
	
	@Autowired
	private GitOpsService gitOpsServ;
	
	@Autowired
	private SharedCompService sharedCompServ;
	
	@Autowired
	private GitService gitServ;
	
	@Autowired
	private CustomFileUtil customeFileUtil;
	
	@Value("${gitlab.repo.user:gitops-bot}")
	private String gitUser;

	@Value("${gitlab.repo.password:}")
	private String gitPass;
	
	@Value("${k8s.cluster.node.afinity.label:default-node-group}")
	private String nodeLabelAfinity;
	
		
	@JmsListener(destination = JMSConfig.REQUEST_QUEUE)
	public void listener(String msg) {
//		log.info("Received message :"+msg);
		String[] data = msg.split("\\|");//data[0]=reqId,data[1]=user,data[2]=password, data[3]=email
		try{
			Thread.sleep(2000);
		}catch (InterruptedException e){
			log.error("Time Interrupted -> {}",e.getMessage());
		}
		ThreadContext.put("reqId", msg);
		this.triggerEnvRep(data);
        ThreadContext.remove("reqId");
		
	}

	/**
	 * This method is used to triiger gitops activity for the environment request.
	 * 1. clone git repo which is registered to be synched with gitops
	 * 2. register gir repo to be synched to gitops (argo)
	 * 3. Create root app yaml and configure argo 
	 * 4. create child app and corresponding manifest
	 * 5. checkin the generated ymls
	 * @param reqId is reqId
	 */
	@Transactional
	private void triggerEnvRep(String[] data) {
		
		String baseDir = "";
				
		try {
			//retrieve specifications from the databse
			Optional<EnvRequest> opEnvReq = envReqRepo.findById(data[0]);
			EnvRequest envReq = opEnvReq.get();
			
			//Get the environment and add it to map which will be used in template
			Optional<Environment> opEnv = envRepo.findById(envReq.getEnvId());
			Environment env = opEnv.get();
			
			//prepare component -> url map. This is required for HAProxy Configuration data
			Map<String, String> compUrlMap = createCompUrlMap(env);
			
			//Get List of Dedicated Components
			List<EnvReqComponent> reqCompDedicatedList = envReq.getReqComp().stream().filter(reqCom -> "dedicated".equalsIgnoreCase(reqCom.getBbComponent().getCompType())).toList();
			if(reqCompDedicatedList==null|| reqCompDedicatedList.isEmpty()) {
				//Assumption is there will be a component for namespace in bb_comp and that is dedicated
//				log.info("No dedicated componenet present like namespace and required componenets");
				throw new EnvReplEngineEx("There is no decicated componenet in the request!!!");
			}
			
			//Get List of shared components
			List<EnvReqComponent> reqCompSharedList = envReq.getReqComp().stream().filter(reqCom -> "shared".equalsIgnoreCase(reqCom.getBbComponent().getCompType())).toList();
			if(reqCompSharedList!=null && !reqCompSharedList.isEmpty()) {
				for(EnvReqComponent sharedComp : reqCompSharedList) {
					if(null!=sharedComp.getBbComponent().getSgaredCompServUrl() && !sharedComp.getBbComponent().getSgaredCompServUrl().isBlank())
						sharedCompServ.handle(env,sharedComp,data[2]);//data[2] contains password which is passed
				}
			}
			
			Map<String, String> dataMap = new HashMap<>();
			dataMap.put("envId", env.getEnvId());
			dataMap.put("env", env.getEnvCode());
			dataMap.put("namespace", env.getEnvNamespace() != null ? env.getEnvNamespace() : env.getEnvCode());
			
			// generate url to clone gitops repo (in lcnc git) for environment components. 
			//NOTE: dataMap should have one env key with value as env code or env name. This will matched and replaced to form the URL
			/*
			 * Assumption is there will be a pre-configured group in lcnc git named as /env-replication-gitops
			 * this will contain repo for each account/environment.
			 * this repo for each environment or account will be created automatically while environment creation takes place after payment success
			 */
			String gitOpsRepoUrl = createGitUrl(dataMap);
			
			//generate image pull secret for the deployment done by the user in git-lcnc
			String dockerConfJson = gitServ.generateImgPullSecretGitLcnc(data);
			dataMap.put("dockerConfJson", dockerConfJson);
			
			//generate imagepull secret for nnp gitlab image repo. User is fixed  and manually created.
			//token will be auto generated using application
			//secret will be generated using that generated git access token for the fixed user.
			
			String dockerConfJsonEiimpGitLab = gitServ.generateImgPullSecretNNPGitLab(env.getEnvCode());
			dataMap.put("dockerConfJsonEiimpGitReg", dockerConfJsonEiimpGitLab);
			
			//add admin userid and password to datamap. This will be used in gitops template to create 3pp deployment with password
			dataMap.put("username", data[1]);
			dataMap.put("password", data[2]);
			dataMap.put("email", data[3]);
			// clone generated repo
			// clone inside a folder as reqId so that each request has separation
			baseDir = location.getFolder() + "/" + data[0];// clone inside a folder named as reqId

			// clone for environment components - lcnc-git
			GitlabActionUtil.cloneFromGitOpsRepo(gitOpsRepoUrl, baseDir, gitUser, gitPass);
			// populate datamap with gitUrl to use templates to generate gitops yml files
			dataMap.put("gitLabUrl", location.getGitlabUrl());//lcnc-git
			dataMap.put("gitLabRepoUrlToReg", gitOpsRepoUrl);//lcnc-git
			
			// populate datamap with the node group selector for k8s node afinity while deploy components.
			dataMap.put("node_group", nodeLabelAfinity);
			
			//populate default CPU, Memory and Storage for Namespace
			Optional<HostPlan> opHostPlan = hostPlanRepo.findById(envReq.getPlanId());
			if (opHostPlan.isPresent()) {
				HostPlan hostPlan = opHostPlan.get();
				dataMap.put("ns_cpu", hostPlan.getHostCPU());
				dataMap.put("ns_mem", hostPlan.getHostMem());
				dataMap.put("ns_storage", hostPlan.getHostStorage());
			} else {
				dataMap.put("ns_cpu", "2");
				dataMap.put("ns_mem", "4Gi");
				dataMap.put("ns_storage", "20Gi");
			}
			
			
			//create gitops related files
			/*
			 * 1. root application file
			 * 2. for each components separate sub application files
			 * 3. manifest files for each components
			 */
			gitOpsServ.createArgoApp(reqCompDedicatedList,dataMap,baseDir,compUrlMap);
			
			//push the files to the repo synched with giops tool
			GitlabActionUtil.pushProject(baseDir, gitOpsRepoUrl, gitUser, gitPass);
			appEventPub.publishEvent(new GitOpsEvent(envReq.getReqId(),"Argo Synch Activated"));
		} catch (GitException | ArgoException e) {
			throw new EnvReplEngineEx(e.getMessage(),e);
		} finally {
			//delete the working folder
			if(!baseDir.isBlank()) {
				customeFileUtil.deleteFile(baseDir);
			}			
			
		}
		
	}
	
	private Map<String, String> createCompUrlMap(Environment env) {
		
		Map<String, String> compUrlMap = new HashMap<String, String>();
		env.getEnvFeatures().forEach(f ->{
			f.getFeatureElements().forEach(fe ->{
				fe.getElementDetails().forEach(fedetail ->{
					fedetail.getChildElementDtls().forEach(fedetailCh ->{
						if(!Objects.isNull(fedetailCh.getCompId())&&!"common".equalsIgnoreCase(fedetailCh.getCompId())) {
							
							String dtlUrl = fedetailCh.getElementDtlURL();
							// Add dummy scheme if missing
					        if (!dtlUrl.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
					        	dtlUrl = "http://" + dtlUrl;
					        }

					        URI uri = URI.create(dtlUrl);
							compUrlMap.put(fedetailCh.getCompId(), uri.getHost());
						}						
					});
				});
			});
		});
		return compUrlMap;
	}

	private String createGitUrl(Map<String, String> dataMap) {
		String url = "";
		for (Map.Entry<String, String> entry : dataMap.entrySet()) {
			//NOTE: ONLY ONE VALUE OF DATAMAP SHOULD MATCH. OTHERWISE THE LOGIC WILL FAIL.
			url = location.getGitlabUrl()+location.getRepopath().replace("${" + entry.getKey() + "}", entry.getValue());
		}
		return url;
	}
	


}
