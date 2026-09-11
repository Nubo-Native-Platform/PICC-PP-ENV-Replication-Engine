package com.nnp.envrep.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author AC
 */

@Entity

@Table(name = "nnp_env_fea_elem_dtl_spec")
@Getter
@Setter
@ToString
public class CHElementDetail implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "dtlspec_id", nullable = false)
    private String chElementDtlId;

    @Column(name = "spec_name", nullable = false)
    private String elementDtlName;

    @Column(name = "spec_shortname")
    private String elementDtlShortName;

    @Column(name = "spec_type")
    private String elementDtlType;

    @Column(name = "spec_home")
    private String elementDtlHome;

    @Column(name = "spec_desc")
    private String elementDtlDesc;

    @Column(name = "spec_url")
    private String elementDtlURL;

    @Column(name = "spec_fatno")
    private String elementDtlFatNo;

    @Column(name = "spec_param1")
    private String demoUrl;

    @Column(name = "spec_param2")
    private String compId;

    @Column(name = "env_fea_elem_dtl_seq")
    private String elementDtlSeq;
    @Transient
    private boolean isAssigned = false;


}
