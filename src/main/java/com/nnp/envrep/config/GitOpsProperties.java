package com.nnp.envrep.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * @author AC
 * @date 21-Apr-2025
 *
 */
@Component
@ConfigurationProperties(prefix = "gitops")
@Getter
@Setter
@ToString
public class GitOpsProperties {
	private String folder = "./gitops";
	private String gitlabUrl = "https://gitlab.example.com";
	private String repopath = "/env-replication-gitops/${env}-synch.git";
	private String template = "./template";
}
