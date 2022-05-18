package com.leimbag.web.demo.controller;

import com.leimbag.demo.core.constant.ServiceConstant;
import com.leimbag.dubbo.user.service.UserService;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author leimbag
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @RequestMapping("/getName")
    public String getUserName(Long uid) {
        String result = userService.getUserName(uid);
        logger.info("查询uid={}的用户名, result={}", uid, result);
        return result;
    }

    @RequestMapping("/getBalance")
    public String getWalletBalance(Long uid) {
        logger.info("查询uid={}的钱包余额, attHeader={}", uid, RpcContext.getContext().getAttachment(ServiceConstant.TAG_GRAY));
        String result = userService.getWalletBalance(uid);
        logger.info("查询uid={}的钱包余额, result={}", uid, result);
        String userName = userService.getUserName(uid);
        logger.info("获取uid={}的用户名为{}", uid, userName);
        return result;
    }

    @RequestMapping("/getUserNameByShutdown")
    public String getUserNameByShutdown(Long uid) {
        String result = userService.getUserNameByShutdown(uid);
        logger.info("查询uid={}的用户名, result={}", uid, result);
        return result;
    }

    @RequestMapping("/test")
    public String test(String name) {
        logger.info("测试:{}", name);
        return "Hello " + name;
    }

    @RequestMapping("/wait")
    public String wait(String name) {
        logger.info("测试:{}", name);
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("wait user: {}", name);
        return "Wait " + name;
    }
}
