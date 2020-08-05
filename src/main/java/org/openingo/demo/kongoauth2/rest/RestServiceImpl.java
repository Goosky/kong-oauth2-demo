package org.openingo.demo.kongoauth2.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * RestServiceImpl
 *
 * @author Qicz
 */
@Service
public class RestServiceImpl implements RestService {

    @Autowired
    RestTemplate restTemplate;

    @Override
    public ResponseEntity<Map> post(String url, MultiValueMap<String, String> param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(param, headers);
        return restTemplate.postForEntity(url, request, Map.class);
    }

    @Override
    public ResponseEntity<Map> get(String url, MultiValueMap<String, String> param) {
        return restTemplate.getForEntity(url, Map.class, param);
    }

    @Override
    public ResponseEntity<Map> get(String url) {
        return restTemplate.getForEntity(url, Map.class);
    }
}
