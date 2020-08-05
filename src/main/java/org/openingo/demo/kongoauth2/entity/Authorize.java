package org.openingo.demo.kongoauth2.entity;

import lombok.Data;

/**
 * Authorize
 *
 * @author Qicz
 */
@Data
public class Authorize {
   String response_type;
   String scope;
   String client_id;
   String user_id;
}
