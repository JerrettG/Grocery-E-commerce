package com.gonsalves.grocery.frontend.frontend.proxy;

import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ProxyController {

    private final RestTemplate restTemplate;
    private final String backendBaseUrl;

    @Autowired
    public ProxyController(RestTemplate restTemplate, @Value("${cloud.gateway.base-url}") String backendBaseUrl) {
        this.restTemplate = restTemplate;
        this.backendBaseUrl = backendBaseUrl;
    }

    @RequestMapping(value = "/api/**", method = { RequestMethod.GET, RequestMethod.DELETE, RequestMethod.POST, RequestMethod.PUT })
    public ResponseEntity<?> proxyRequest(HttpServletRequest request, @RequestBody(required = false) String requestBody, @RequestHeader HttpHeaders requestHeaders) {
        String forwardUrl = backendBaseUrl + request.getRequestURI();
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, requestHeaders);

        return restTemplate.exchange(forwardUrl, HttpMethod.valueOf(request.getMethod()), httpEntity, String.class);
    }
}
