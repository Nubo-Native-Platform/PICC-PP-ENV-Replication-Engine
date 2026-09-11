package com.nnp.envrep.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author AC
 */

@Entity

@Table(name = "nnp_env_fea_elem")
@Getter
@Setter
@ToString
public class FeatureElement implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id

    @Column(name = "elem_id", nullable = false)
    private String elementId;

    @Column(name = "elem_name", nullable = false)
    private String elementName;

    @Column(name = "elem_type", nullable = false)
    private String elementType;

    @Column(name = "elem_desc")
    private String elementDesc;

    @Column(name = "elem_page")
    private String elementPage;

    @Column(name = "iimp_env_fea_elem_seq")
    private String feaSeq;

	@OneToMany//(mappedBy = "feaElement", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "elem_id", nullable = false)
	private List<ElementDetail> elementDetails = new ArrayList<>();
    @Transient
    private boolean isAssigned = false;

}
