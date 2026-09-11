/**
 * EnvBBCompSpec.java
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * EnvBBCompSpec.java
 *
 * @author AC
 * @date 23-Apr-2025
 */
@Entity
@Table(name = "env_compspec")
@Getter
@Setter
public class EnvBBCompSpec implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "env_specid")
	private String specid;

	@Column(name = "env_specname")
	private String specName;
	@Column(name = "env_spectype")
	private String specType;
	@Column(name = "env_specvalidation")
	private String specvalidation;
	@Column(name = "env_specdesc")
	private String specdesc;
	@Column(name = "env_specvalues")
	private String specvalues;
	@Column(name = "env_compstatus")
	private String compstatus;
	@Column(name = "env_spec_var_name")
	private String tmplSpecVarName;
	@Column(name = "is_editable")
	private boolean isEditable;

	@ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
	@JoinColumn(name = "env_compid", nullable = false)
	private EnvBBComponent bbComponent;
	@OneToMany(mappedBy = "bbCompSpec", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<EnvReqSpec> reqSpec = new ArrayList<EnvReqSpec>();


}
