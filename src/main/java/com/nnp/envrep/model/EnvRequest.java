/**
 * EnvRequest.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
package com.nnp.envrep.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * EnvRequest.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
@Entity
@Table(name = "env_req")
@Setter
@Getter
@ToString
public class EnvRequest implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	/*
	 * @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
	 * "env_generic_id_seq")
	 * 
	 * @SequenceGenerator(name = "env_generic_id_seq", sequenceName =
	 * "env_generic_id_seq", allocationSize = 1)
	 */
	@Column(name = "env_reqid")
	
	private String reqId;
	@Column(name = "env_reqcapdt")
	private Timestamp reqCapDT;

	@Column(name = "env_reqcomdt")
	private Timestamp reqComDT;

	@Column(name = "env_reqdtl")
	private String reqDtl;

	@Column(name = "env_reqresubdt")
	private Timestamp reqReSubDT;

	@Column(name = "env_id")
	private String envId;

	@Column(name = "env_reqstatdtl")
	private String reqStatDtl;

	@Column(name = "env_reqstatus")
	private String reqStatus;

	@Column(name = "env_reqtitle")
	private String reqTitle;
	
	@Column(name = "env_hostplanid")
	private long planId;

	@OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<EnvReqComponent> ReqComp = new ArrayList<EnvReqComponent>();

	@OneToMany(mappedBy = "envReq", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<EnvReqCommunication> reqCommunication = new ArrayList<EnvReqCommunication>();


}
