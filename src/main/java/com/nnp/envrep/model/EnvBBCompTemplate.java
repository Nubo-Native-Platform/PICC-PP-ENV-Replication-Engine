/**
 * EnvBBCompTemplate.java
 *
 * @author AC
 * @date 12-May-2025
 */
package com.nnp.envrep.model;

import java.io.Serializable;
import java.util.List;

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
 * EnvBBCompTemplate.java
 *
 * @author AC
 * @date 12-May-2025
 */
@Entity
@Table(name = "env_comp_template")
@Getter
@Setter
public class EnvBBCompTemplate implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -182166401830020232L;
	@Id
	@Column(name = "template_id")
	private String tmplId;
	
	@Column(name = "comp_tmplate")
	private String template;
	
	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "file_path")
	private String filePath;
	
	@Column(name = "template_type")
	private String tmplType;
	
	@Column(name = "created_by")
	private String createdBy;
	
	@Column(name = "status")
	private String status;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
	@JoinColumn(name = "env_compid", nullable = false)
	private EnvBBComponent bbComponent;

}
