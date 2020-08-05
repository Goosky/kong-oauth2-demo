package org.openingo.demo.kongoauth2.entity;

import lombok.Data;

/**
 * User
 *
 * @author Qicz
 */
@Data
public class User {
    String userId;
    String username;
    String password;
    String scope;
    String grant_type = "password";
    String client_id;
    String client_secret;
    String callback = null;
}
