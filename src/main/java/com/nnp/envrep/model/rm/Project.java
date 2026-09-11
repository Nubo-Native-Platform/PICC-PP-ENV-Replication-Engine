/**
 * Project.java
 *
 * @author AC
 * @date 30-Apr-2025
 */
package com.nnp.envrep.model.rm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * Project.java
 *
 * @author AC
 * @date 30-Apr-2025
 */


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "identifier",
        "description",
        "homepage",
        "status",
        "is_public",
        "inherit_members",
        "created_on",
        "updated_on"
})

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Project {
	
    @JsonProperty("id")
    public Integer id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("identifier")
    public String identifier;
    @JsonProperty("description")
    public Object description;
    @JsonProperty("homepage")
    public String homepage;
    @JsonProperty("status")
    public Integer status;
    @JsonProperty("is_public")
    public Boolean isPublic;
    @JsonProperty("inherit_members")
    public Boolean inheritMembers;
    @JsonProperty("created_on")
    public String createdOn;
    @JsonProperty("updated_on")
    public String updatedOn;

}
