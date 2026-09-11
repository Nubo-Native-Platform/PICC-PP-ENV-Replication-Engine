/**
 * EnvBBComponent.java
 *
 * @author AC
 * @date 23-Apr-2025
 */
package com.nnp.envrep.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * EnvBBComponent.java
 *
 * @author AC
 * @date 23-Apr-2025
 */
@Entity
@Table(name = "env_bbcomp")
@Getter
@Setter
public class EnvBBComponent implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "env_compid")
	private String compId;
	@Column(name = "env_compname")
	private String compName;
	@Column(name = "env_compdesc")
	private String compDesc;
	@Column(name = "env_compstatus")
	private String compStatus;
	@Column(name = "env_comp_type")
	private String compType;
	@Column(name = "item_seq")
    private Short itemSeq;
	@Column(name = "env_platform")
	private String envPlatform;
	@Column(name = "env_git_path")
	private String envGitPath;
	@Column(name = "env_git_accesstoken")
	private String envGitToken;
	@Column(name = "shared_comp_serv_url")
	private String sgaredCompServUrl;
	@Column(name = "k8s_comp_name")
	private String k8sCompName;
	@Column(name = "is_haproxy")
	private boolean isProxyExpose;

	@OneToMany(mappedBy = "bbComponent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<EnvBBCompSpec> bbCompSpec = new ArrayList<EnvBBCompSpec>();

	@OneToMany(mappedBy = "bbComponent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<EnvReqComponent> reqComponent = new ArrayList<EnvReqComponent>();
	
	@OneToMany(mappedBy = "bbComponent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<EnvBBCompTemplate> bbCompTempl = new ArrayList<EnvBBCompTemplate>();



}
