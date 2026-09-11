/**
 * GitService.java
 *
 * @author AC
 * @date 29-May-2025
 */
package com.nnp.envrep.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nnp.envrep.model.rm.Project;

import lombok.extern.slf4j.Slf4j;

/**
 * GitService.java
 *
 * @author AC
 * @date 29-May-2025
 */
@Service
@Slf4j
public class GitService {
	
	@Qualifier("webClientGitIntServ")
	@Autowired
	private WebClient webClientGitIntServ;
	
	/* @Value("${imagepull.secret.dockerconfigjson}") */
	private String dockerconfigjson = "{\"auths\":{\"${imageRegistryUrl}\":{\"username\":\"${user}\",\"password\":\"${gitpat}\",\"auth\":\"${tokenEncode}\"}}}";
	
	@Value("${gitlcnc.imageregistry.url:https://registry.example.com}")
	private String gitlcncCIRegistryURL;
	
	@Value("${gitlab.nnp.imageregistry.url:https://registry.example.com}")
	private String nnpGitCIRegistryURL;
	
	@Value("${gitlab.nnp.user:nnp-support}")
	private String nnpGitlabUser;
	
	/**
	 * @param data - data[0] is reqId and data[1] is admin user
	 * @return
	 */
	public String generateImgPullSecretGitLcnc(String[] data) {
		
		String dockerconfigjsonLCNC = dockerconfigjson;
		Map<String, String> dataMap = new HashMap<>();
		String userName = data[1];
		
		dataMap.put("user", userName);		
		String gitPersonalAccessToken = webClientGitIntServ.get().uri(uriBuilder -> uriBuilder.path("/api/getLcncGitPAT/{userId}").build(userName)).retrieve().bodyToMono(String.class).block();
		dataMap.put("gitpat", gitPersonalAccessToken);
		//generate base 64 encoding of username and the token
		String tokenEncode = Base64.getEncoder().encodeToString((userName+":"+gitPersonalAccessToken).getBytes(java.nio.charset.StandardCharsets.UTF_8));
		dataMap.put("tokenEncode", tokenEncode);
		dataMap.put("imageRegistryUrl", gitlcncCIRegistryURL);
		
		
		for (Map.Entry<String, String> entry : dataMap.entrySet()) {
			dockerconfigjsonLCNC = dockerconfigjsonLCNC.replace("${" + entry.getKey() + "}", entry.getValue());
		}

		return Base64.getEncoder().encodeToString(dockerconfigjsonLCNC.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	/**
	 * @return
	 */
	public String generateImgPullSecretNNPGitLab(String env) {
		
		String dockerconfigjsonEiimpGit = dockerconfigjson;
		
		Map<String, String> dataMap = new HashMap<>();
				
		dataMap.put("user", nnpGitlabUser);		
		String gitPersonalAccessToken = webClientGitIntServ.get().uri(uriBuilder -> uriBuilder.path("/nnp/api/getNNPGitLabPAT/{userId}/{env}").build(nnpGitlabUser,env)).retrieve().bodyToMono(String.class).block();
		dataMap.put("gitpat", gitPersonalAccessToken);
		//generate base 64 encoding of username and the token
		String tokenEncode = Base64.getEncoder().encodeToString((nnpGitlabUser+":"+gitPersonalAccessToken).getBytes(java.nio.charset.StandardCharsets.UTF_8));
		dataMap.put("tokenEncode", tokenEncode);
		dataMap.put("imageRegistryUrl", nnpGitCIRegistryURL);
		
		
		for (Map.Entry<String, String> entry : dataMap.entrySet()) {
			dockerconfigjsonEiimpGit = dockerconfigjsonEiimpGit.replace("${" + entry.getKey() + "}", entry.getValue());
		}
		return Base64.getEncoder().encodeToString(dockerconfigjsonEiimpGit.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

}
