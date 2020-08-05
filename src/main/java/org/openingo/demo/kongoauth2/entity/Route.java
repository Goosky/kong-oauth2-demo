package org.openingo.demo.kongoauth2.entity;

import lombok.Data;

/**
 * Route
 *
 * @author Qicz
 */
@Data
public class Route {
    String matchUrl;
    String method;
    String path;
    String sysCode;
}
