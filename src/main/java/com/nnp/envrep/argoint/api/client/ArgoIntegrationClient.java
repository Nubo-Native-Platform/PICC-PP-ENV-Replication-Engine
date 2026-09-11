/**
 * ArgoIntegrationClient.java
 *
 * @author AC
 * @date 06-May-2025
 */
package com.nnp.envrep.argoint.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.nnp.envrep.config.ArgoFeignConfig;
import com.nnp.envrep.model.argo.ArgoGitRepoReg;

/**
 * ArgoIntegrationClient.java
 *
 * @author AC
 * @date 06-May-2025
 */
@FeignClient(name = "${feign.name:argo-client}", url = "${feign.url:http://localhost:8080}${feign.url.api:/api/v1}", configuration = ArgoFeignConfig.class)
public interface ArgoIntegrationClient {
	
    @PostMapping(value = "/session", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> createJWTToken(@RequestBody String jsonPayLoad);
    
    @PostMapping(value = "/applications", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> createApplication(@RequestHeader("Authorization") String token,@RequestBody String jsonPayLoad);
    
    @PostMapping(value = "/repositories", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> createRepo(@RequestHeader("Authorization") String token,@RequestBody ArgoGitRepoReg argoRepo);

    @DeleteMapping(value = "/repositories/{repoUrl}")
    ResponseEntity<String> deleteRepo(@RequestHeader("Authorization") String token, @PathVariable("repoUrl") String repoUrl);


}
