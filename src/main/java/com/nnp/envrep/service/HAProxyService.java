package com.nnp.envrep.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nnp.envrep.event.GitOpsEvent;
import com.nnp.envrep.exception.HAProxyException;
import com.nnp.envrep.model.EnvBBCompSpec;
import com.nnp.envrep.model.EnvBBComponent;
import com.nnp.envrep.model.EnvReqComponent;
import com.nnp.envrep.model.EnvRequest;
import com.nnp.envrep.model.Environment;
import com.nnp.envrep.model.haproxy.HAProxyIn;
import com.nnp.envrep.repo.EnvRepo;
import com.nnp.envrep.repo.EnvReqRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HAProxyService {

	@Qualifier("webClientHAProxyInt")
	@Autowired
	private WebClient webClientHAProxy;

	@Autowired
	private EnvReqRepo envReqRepo;

	@Autowired
	private EnvRepo envRepo;

	@Value("${haproxy.base.domain:nnp.nubons.com}")
	private String baseDomain;

	public void registerCompHaproxy(GitOpsEvent event) {
		// retrieve components associated with request. Name of the components will be
		// present in ticket
		EnvRequest envReq = envReqRepo.findById(event.getReqId()).get();
		// Get The Environment against this request.
		Environment env = envRepo.findById(envReq.getEnvId())
				.orElseThrow(() -> new HAProxyException("Environment not found for EnvID - " + envReq.getEnvId()));

		List<EnvReqComponent> envReqComponenets = envReq.getReqComp();
		int cnt = 0;
		for (EnvReqComponent reqComp : envReqComponenets) {
			// call haproxy registration service for each component
			EnvBBComponent bbComp = reqComp.getBbComponent();
//			log.info("bbComp {}  need to expose ? "+bbComp.isProxyExpose(),bbComp.getCompName());
			if (bbComp.isProxyExpose()) {// if Proxy Expose is true that means the component need to expose in HA Proxy
//				log.info("Going to HAPROXY call ");
				EnvBBCompSpec bbSpec =  bbComp.getBbCompSpec().stream().filter(spec -> "internal_port".equalsIgnoreCase(spec.getTmplSpecVarName())).findFirst().orElseThrow(() -> new HAProxyException("Internal Port not set for the component - "+bbComp.getCompName()));
				String envName = (env.getEnvCode() != null ? env.getEnvCode() : env.getEnvId()).toLowerCase().trim();
				String compName = bbComp.getK8sCompName();
				if (compName == null || compName.isBlank()) {
					compName = bbComp.getCompName().toLowerCase().trim().replaceAll("[^a-z0-9-]", "-");
				}

				HAProxyIn proxy = new HAProxyIn();
				proxy.setCompName(compName);
				proxy.setLineIndex(cnt++);
				proxy.setNamespace(envName);
				proxy.setParentFE("http_front");
				proxy.setDomain(String.format("%s-%s.%s", compName, envName, baseDomain));
				proxy.setInternalPort(Integer.parseInt(bbSpec.getSpecvalues().trim()));
//				ResponseEntity<String> resp = webClientHAProxy.post().uri("/register").contentType(MediaType.APPLICATION_JSON).bodyValue(proxy).retrieve().bodyToMono(ResponseEntity.class).block();
			}

		}

	}

}
