package org.openingo.demo.kongoauth2.controller;

import org.openingo.demo.kongoauth2.entity.Route;
import org.openingo.demo.kongoauth2.rest.RestService;
import org.openingo.jdkits.validate.ValidateKit;
import org.openingo.spring.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * RouteController
 *
 * @author Qicz
 */
@RestController
public class RouteController {

    String kongProxyServicesUrl = "http://192.168.200.204:8001/services";
    String kongProxyRoutesUrl = "http://192.168.200.204:8001/services/%s/routes";

    @Autowired
    RestService restService;

    @PostMapping("/addRoute")
    public ModelAndView addRoute(Route route) {
        boolean valid = false;
        // services
        MultiValueMap<String, String> servicesParam = new LinkedMultiValueMap<>();
        String matchUrl = route.getMatchUrl();
        if (ValidateKit.isNull(matchUrl)) {
            throw new ServiceException("matchUrl必须提供。");
        }
        servicesParam.set("url", matchUrl);
        ResponseEntity<Map> post = this.restService.post(kongProxyServicesUrl, servicesParam);
        if (!post.getStatusCode().equals(HttpStatus.CREATED)) {
            throw new ServiceException("创建service失败"+post.getBody().get("message"));
        }
        Object id = post.getBody().get("id");
        // route
        MultiValueMap<String, String> routesParam = new LinkedMultiValueMap<>();
        String method = route.getMethod();
        if (ValidateKit.isNotNull(method)) {
            valid = true;
            routesParam.set("methods", method);
        }
        String path = route.getPath();
        if (ValidateKit.isNotNull(path)) {
            valid = true;
            routesParam.set("paths[]", path);
        }
        String sysCode = route.getSysCode();
        if (ValidateKit.isNotNull(sysCode)) {
            valid = true;
            routesParam.set("headers.sysCode", sysCode);
        }
        if (!valid) {
            throw new ServiceException("参数method、path至少得选择一个。");
        }

        ResponseEntity<Map> routePost = this.restService.post(String.format(kongProxyRoutesUrl, id), routesParam);
        if (!routePost.getStatusCode().equals(HttpStatus.CREATED)) {
            throw new ServiceException("创建Routes失败"+routePost.getBody().get("message"));
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.getSession().setAttribute("addrouteok", "=> success!!!");
        return new ModelAndView("index.html");
    }
}
