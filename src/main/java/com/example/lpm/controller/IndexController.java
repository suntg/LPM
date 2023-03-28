package com.example.lpm.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.lpm.v3.domain.vo.LoginVO;
import com.example.lpm.service.UserService;
import com.example.lpm.util.IpUtil;
import com.example.lpm.v3.common.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class IndexController {

    @Resource
    private HttpServletRequest request;
    @Resource
    private UserService userService;
    @Value("${spring.sa-token.token-name}")
    private String tokenName;

    /**
     * 来登录页
     *
     * @return
     */
    @GetMapping(value = {"/", "/login"})
    public String loginPage() {
        return "login";
    }

    @PostMapping("/signIn")
    public String main(LoginVO user, HttpSession session, Model model) { // RedirectAttributes
        String ip = IpUtil.getIpAddr(request);
        try {
            String token = userService.login(user.getUsername(), user.getPassword(), ip);
            // 把登陆成功的用户保存起来
            session.setAttribute("loginUser", user);
            session.setAttribute("token", token);
            return "redirect:/main.html";
        } catch (BizException e) {
            model.addAttribute("msg", e.getMessage());
            // 回到登录页面
            return "login";
        }
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public String logout() {
        Cookie[] cookies = request.getCookies();
        String token = getValue(cookies, tokenName);
        log.info("logout token:{}", token);
        if (StringUtils.isNotEmpty(token)) {
            StpUtil.logoutByTokenValue(token);
        }
        return "login";
    }

    /**
     * 去main页面
     *
     * @return
     */
    @GetMapping("/main.html")
    public String mainPage(HttpSession session, Model model) {
         try {
         if (!StpUtil.isLogin()) {
         return "redirect:login";
         }
         } catch (QueryTimeoutException e) {
         return "redirect:login";
         } catch (Exception e) {
         return "redirect:login";
         }
         if (!StpUtil.isLogin()) {
            return "redirect:login";
        }
        return "main";

    }

    /**
     * 去ipaddr_list页面
     *
     * @return
     */
    @GetMapping("/ipaddr_list")
    public String ipaddrList(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/ipaddr_list";
    }

    /**
     * 去job_set页面
     *
     * @return
     */
    @GetMapping("/job_set")
    public String jobSet(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/job_set";
    }

    /**
     * 去log_query页面
     *
     * @return
     */
    @GetMapping("/log_query")
    public String logQuery(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/log_query";
    }

    /**
     * 去rola_ip页面
     *
     * @return
     */
    @GetMapping("/rola_ip")
    public String rolaIp(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/rola_ip";
    }

    /**
     * 去rola_proxy页面
     *
     * @return
     */
    @GetMapping("/rola_proxy")
    public String rolaProxy(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/rola_proxy";
    }

    /**
     * 去ip_include页面
     *
     * @return
     */
    @GetMapping("/ip_include")
    public String ipInclude(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/ip_include";
    }

    /**
     * 去ip_query页面
     *
     * @return
     */
    @GetMapping("/ip_query")
    public String ipQuery(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/ip_query";
    }

    /**
     * 去ip_recruit页面
     *
     * @return
     */
    @GetMapping("/ip_recruit")
    public String ipRecruit(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/ip_recruit";
    }

    /**
     * 去ip_proxy页面
     *
     * @return
     */
    @GetMapping("/ip_proxy")
    public String ipProxy(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/ip_proxy";
    }

    /**
     * 去operation_log页面
     *
     * @return
     */
    @GetMapping("/operation_log")
    public String operationLog(HttpSession session, Model model) {
        if (!StpUtil.isLogin()) {
            return "login";
        }
        return "table/operation_log";
    }

    /**
     * 从Cookie中获取频道编码channelCode
     *
     * @param cookies
     * @return
     */
    protected String getValue(Cookie[] cookies, String key) {
        String value = null;
        if (null != cookies && cookies.length > 0) {
            for (Cookie c : cookies) {
                if (key.equals(c.getName())) {
                    value = c.getValue();
                    break;
                }
            }
        }

        return value;
    }

}
