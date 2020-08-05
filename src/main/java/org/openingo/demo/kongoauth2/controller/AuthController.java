package org.openingo.demo.kongoauth2.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openingo.demo.kongoauth2.entity.Authorize;
import org.openingo.demo.kongoauth2.entity.User;
import org.openingo.demo.kongoauth2.rest.RestService;
import org.openingo.java.lang.ThreadLocalX;
import org.openingo.jdkits.coding.Base64Kit;
import org.openingo.jdkits.encryption.AesKit;
import org.openingo.jdkits.hash.HashKit;
import org.openingo.jdkits.validate.ValidateKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * AuthController
 *
 * @author Qicz
 */
@RestController
@Slf4j
public class AuthController {

    @Autowired
    RestService restService;

    volatile Boolean logged = false;
    String provision_key = "mEsR3yjx2pl2yCf57QsJQ8VI3kOgrMIU";
    String kongProxyOAuth2Url = "https://192.168.200.204:8443/weather/oauth2/";
    String kongProxyClientUrl = "http://192.168.200.204:8001/oauth2?client_id=%s";
    String kongProxyOAuth2AuthCodeUrl = kongProxyOAuth2Url + "authorize";
    String kongProxyOAuth2TokenUrl = kongProxyOAuth2Url + "token";

    private String aesKey = HashKit.md5("123");

    @PostMapping("/doLogin")
    public ModelAndView doLogin(User user) {
        if (!"zcq".equals(user.getUsername())
                || !"123".equals(user.getPassword())) {
            return new ModelAndView("error0.html");
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        this.logged(request.getSession());

        String callback = user.getCallback();
        if (ValidateKit.isNotEmpty(callback)) {
            String userId = Base64Kit.encode(AesKit.encrypt("1012".getBytes(), aesKey));
            callback = "redirect:"+ callback + "&user_id=" + userId;
            return new ModelAndView(callback);
        }
        return new ModelAndView("index.html");
    }

    @PostMapping("/doPassword")
    public ModelAndView doPassword(User user) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.set("client_id", user.getClient_id());
        param.set("client_secret", "client_secret");
        param.set("grant_type", "password");
        param.set("scope", user.getScope());
        param.set("provision_key", provision_key);
        param.set("authenticated_userid", "1012");
        param.set("username", user.getUsername());
        param.set("password", user.getPassword());

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return getModelAndView(request, param);
    }

    private ModelAndView getModelAndView(HttpServletRequest request, MultiValueMap<String, String> param) {
        ResponseEntity<Map> post = this.restService.post(kongProxyOAuth2TokenUrl, param);
        if (!post.getStatusCode().equals(HttpStatus.OK)) {
            return new ModelAndView("error0.html");
        }
        Map body = post.getBody();
        Object access_token = body.get("access_token");
        Object refresh_token = body.get("refresh_token");
        HttpSession session = request.getSession();
        session.setAttribute("access_token", access_token);
        session.setAttribute("refresh_token", refresh_token);
        this.logged(session);
        return new ModelAndView("redirect:index.html");
    }

    private void logged(HttpSession session) {
        logged = true;
        session.setAttribute("logged", "=> logged!!!");
        session.removeAttribute("logoutok");
    }

    @SneakyThrows
    @GetMapping("/authorize")
    public ModelAndView authorize(Authorize authorize) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (!logged) {
            request.setAttribute("callback", request.getRequestURL()+"?"+request.getQueryString());
            return new ModelAndView("login.html");
        }

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.set("client_id", authorize.getClient_id());
        param.set("response_type", "code");
        param.set("scope", authorize.getScope());
        param.set("provision_key", provision_key);
        // 可以考虑其他方式处理userId的获取
        String user_id = authorize.getUser_id();
        if (ValidateKit.isNotNull(user_id)) {
            user_id = AesKit.decryptToStr(Base64Kit.decode(user_id.replace(' ', '+')), aesKey);
        } else {
            user_id = "1012";
        }
        param.set("authenticated_userid", user_id);
        ResponseEntity<Map> post = this.restService.post(kongProxyOAuth2AuthCodeUrl, param);
        if (!post.getStatusCode().equals(HttpStatus.OK)) {
            return new ModelAndView("error0.html");
        }
        Map body = post.getBody();
        String redirect_uri = body.get("redirect_uri").toString() + "&client_id=" + authorize.getClient_id();
        return new ModelAndView("redirect:" + redirect_uri);
    }

    @GetMapping("/callback")
    public ModelAndView callback(String code, String client_id) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.getSession().setAttribute("code", code);
        request.getSession().setAttribute("authorizeok", "=> authorize success!!!");

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.set("client_id", client_id);
// 可以通过以下方式获取配置的client_secret信息
//        Map forObject = this.restTemplate.getForObject(String.format(kongProxyClientUrl, client_id), Map.class);
//        Object data = forObject.get("data");
//        if (data instanceof List) {
//            Map o = (Map)((List) data).get(0);
//        }
        param.set("client_secret", "client_secret");
        param.set("grant_type", "authorization_code");
        param.set("code", code);
        param.set("redirect_uri", "http://192.168.110.82:8080/callback");
        return getModelAndView(request, param);
    }

    @GetMapping("/refreshToken")
    public ModelAndView refreshToken(@RequestParam("refresh_token") String refreshToken,
                                     @RequestParam("client_id") String clientId) {
        if (ValidateKit.isNull(refreshToken)
                || "null".equals(refreshToken)) {
            return new ModelAndView("error0.html");
        }
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.set("client_id", clientId);

// 可以通过以下方式获取配置的client_secret信息
//        Map forObject = this.restTemplate.getForObject(String.format(kongProxyClientUrl, client_id), Map.class);
//        Object data = forObject.get("data");
//        if (data instanceof List) {
//            Map o = (Map)((List) data).get(0);
//        }

        param.set("client_secret", "client_secret");
        param.set("grant_type", "refresh_token");
        param.set("refresh_token", refreshToken);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        return getModelAndView(request, param);
    }

    @GetMapping("/logout")
    public ModelAndView logout() {
        this.doLogout();
        return new ModelAndView("index.html");
    }

    private void doLogout() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        logged = false;
        // 删除旧的状态
        session.removeAttribute("logged");
        session.removeAttribute("code");
        session.removeAttribute("authorizeok");
        session.removeAttribute("access_token");
        session.removeAttribute("refresh_token");
        // 设置注销状态
        session.setAttribute("logoutok", "=> logout success!!!");
    }
}
