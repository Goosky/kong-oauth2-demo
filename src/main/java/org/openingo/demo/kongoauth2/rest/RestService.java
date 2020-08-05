package org.openingo.demo.kongoauth2.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * RestService
 *
 * @author Qicz
 */
public interface RestService {

    ResponseEntity<Map> post(String url, MultiValueMap<String, String> param);

    ResponseEntity<Map> get(String url, MultiValueMap<String, String> param);

    ResponseEntity<Map> get(String url);
}
