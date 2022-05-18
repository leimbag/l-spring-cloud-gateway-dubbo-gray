package com.leimbag.dubbo.user.service.impl;

import com.leimbag.dubbo.user.service.UserService;
import com.leimbag.dubbo.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author leimbag
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WalletService walletService;

    @Override
    public String getUserName(Long uid) {
        logger.info("获取用户名，uid:{}", uid);
        return "TestUser:" + uid;
    }

    @Override
    public String getUserAlias(Long uid) {
        logger.info("获取用户别名，uid:{}", uid);
        return "AliasUser:" + uid;
    }

    @Override
    public String getWalletBalance(Long uid) {
        String userAlias = getUserAlias(uid);
        logger.info("获取用户别名，uid:{}, userAlias={}", uid, userAlias);
        String wallet = walletService.getWallet(uid);
        logger.info("获取用户钱包余额：{}", wallet);
        String walletName = walletService.getWalletName(uid);
        logger.info("获取用户钱包名称：{}", walletName);
        return wallet;
    }

    @Override
    public String getUserNameByShutdown(Long uid) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("等待十秒, uid={}", uid);
        return "TestUser:" + uid;
    }

}
