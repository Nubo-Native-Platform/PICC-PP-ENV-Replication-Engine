/**
 * Role.java
 *
 * @author AC
 * @date 30-Apr-2025
 */
package com.nnp.envrep.model.rm;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Role.java
 *
 * @author AC
 * @date 30-Apr-2025
 */
@Getter
@Setter
@ToString
public class Role {
	@JsonProperty("id")
	private Integer id;
	@JsonProperty("name")
	private String name;

}
