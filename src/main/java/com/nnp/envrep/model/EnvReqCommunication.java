/**
 * EnvReqCommunication.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
package com.nnp.envrep.model;

import java.io.Serializable;
import java.sql.Timestamp;

import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * EnvReqCommunication.java
 *
 * @author AC
 * @date 22-Apr-2025
 */
@Entity
@Table(name = "env_reqcomm")
@Getter
@Setter
@ToString
public class EnvReqCommunication implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cn_hosting_generic_id_seq")
    @SequenceGenerator(name = "cn_hosting_generic_id_seq", sequenceName = "cn_hosting_generic_id_seq", allocationSize = 1)
    @Column(name = "env_reqcommid")
    private long envReqCommId;

    @ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "env_reqid", nullable = true)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    private EnvRequest envReq;

    @Column(name = "env_date_start")
    private Timestamp envDateStart;

    @Column(name = "env_date_end")
    private Timestamp envDateEnd;

    @Column(name = "env_commtype")
    private String envCommunicationType;

    @Column(name = "env_name")
    private String envName;

    @Column(name = "env_comm")
    private String envCommunication;

    @Column(name = "status")
    private String activityStatus;


}
