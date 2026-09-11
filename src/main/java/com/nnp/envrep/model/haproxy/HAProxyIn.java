package com.nnp.envrep.model.haproxy;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HAProxyIn {
	
	private String compName;
	private int internalPort;
	private String namespace;
	private String domain;
	private String parentFE;
	private int lineIndex;

}
