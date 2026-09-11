package com.nnp.envrep.model.rm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "login",
        "admin",
        "firstname",
        "lastname",
        "mail",
        "created_on",
        "last_login_on",
        "api_key",
        "status"
})
@Setter
@Getter
@ToString
public class User {
    @JsonProperty("id")
    public Integer id;
    @JsonProperty("login")
    public String login;
    @JsonProperty("admin")
    public Boolean admin;
    @JsonProperty("firstname")
    public String firstname;
    @JsonProperty("lastname")
    public String lastname;
    @JsonProperty("mail")
    public String mail;
    @JsonProperty("created_on")
    public String createdOn;
    @JsonProperty("last_login_on")
    public Object lastLoginOn;
    @JsonProperty("api_key")
    public String apiKey;
    @JsonProperty("status")
    public Integer status;
}
