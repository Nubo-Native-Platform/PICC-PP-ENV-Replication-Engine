package com.nnp.envrep.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AC
 */

@Entity
@Table(name = "nnp_plan")
@Getter
@Setter
public class HostPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cn_hosting_generic_id_seq")
    @SequenceGenerator(name = "cn_hosting_generic_id_seq", sequenceName = "cn_hosting_generic_id_seq", allocationSize = 1)
    @Column(name = "host_planid")
    private long hostPlanid;

    @Column(name = "host_plname")
    private String hostPlanName;

    @Column(name = "host_pldesc")
    private String hostPlanDesc;

    @Column(name = "host_plcatagory")
    private String hostPlanCatagory;

    @Column(name = "host_plstatus")
    private String hostPlanStatus;

    @Column(name = "host_plspotlgt")
    private String hostPlanSpotlight;

    @Column(name = "host_plbasepr")
    private String hostPlanBasePr;

    @Column(name = "host_maxpod")
    private Short hostMaxPod;

    @Column(name = "host_maxbandw")
    private BigInteger hostMaxBandw;

    @Column(name = "host_maxaction")
    private String hostMaxAction;

    @Column(name = "host_minduration")
    private Integer hostMinDuration;
    
    @Column(name = "host_maxpct")
    private Integer hostMaxPCT;
    
    @Column(name = "host_node")
    private String hostNode;
    
    @Column(name = "host_cpu")
    private String hostCPU;
    
    @Column(name = "host_mem")
    private String hostMem;
    
    @Column(name = "host_storage")
    private String hostStorage;
    
    @Column(name = "active")
    private boolean isActive;

    @Column(name = "item_seq")
    private Short itemSeq;

    @Column(name = "country_id")     /** Ideally a reference to host_orgtaxid field of host_orgtax table */
    private String countryId;

    @Column(name = "host_plandtl_pagelink")
    private String hostPlanDtlPageLink;
    
    @Column(name = "host_default_dct")
    private BigDecimal hostDefaultDct;

	/*
	 * @OneToOne(mappedBy = "hostPlan") private HostAccPlan hostAccPlan;
	 */
	/*
	 * @OneToMany(mappedBy = "hostPlan", fetch = FetchType.LAZY, cascade =
	 * CascadeType.ALL) private List<HostAccPlan> hostAccPlans;
	 */

	/*
	 * @OneToMany(mappedBy = "hostPlan")
	 * 
	 * @JsonManagedReference private List<HostPlanComp> hostPlanComps;
	 */
}