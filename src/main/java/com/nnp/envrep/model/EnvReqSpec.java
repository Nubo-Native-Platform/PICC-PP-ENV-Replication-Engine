/**
 * EnvReqSpec.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
package com.nnp.envrep.model;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * EnvReqSpec.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
@Entity
@Table(name = "env_reqspec")
@Getter
@Setter
public class EnvReqSpec implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "env_reqspecid")
	/*
	 * @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
	 * "env_generic_id_seq")
	 * 
	 * @SequenceGenerator(name = "env_generic_id_seq", sequenceName =
	 * "env_generic_id_seq", allocationSize = 1)
	 */
	private String reqSpecId;

	@Column(name = "env_specvalue")
	private String specValue;

	@ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
	@JoinColumn(name = "env_reqcompid", nullable = false)
	private EnvReqComponent reqComponent;

	@ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
	@JoinColumn(name = "env_specid", nullable = false)
	private EnvBBCompSpec bbCompSpec;

}
