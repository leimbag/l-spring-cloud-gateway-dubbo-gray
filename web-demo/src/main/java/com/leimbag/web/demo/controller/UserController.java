package com.leimbag.web.demo.controller;

import com.leimbag.demo.core.constant.ServiceConstant;
import com.leimbag.dubbo.user.service.UserService;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return result;
    }

    @RequestMapping("/getUserNameByShutdown")
    public String getUserNameByShutdown(Long uid) {
        String result = userService.getUserNameByShutdown(uid);
        logger.info("查询uid={}的用户名, result={}", uid, result);
        return result;
    }
}
