/**
 * ArgoRepo.java
 *
 * @author AC
 * @date 07-May-2025
 */
package com.nnp.envrep.model.argo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ArgoRepo.java
 *
 * @author AC
 * @date 07-May-2025
 */
@Setter
@Getter
@ToString
public class ArgoGitRepoReg {
	@JsonProperty("type")
	private String type;
	@JsonProperty("repo")
	private String repo;
	@JsonProperty("username")
	private String gitUser;
	@JsonProperty("password")
	private String gitPass;
	@JsonProperty("project")
	private String project;

}
