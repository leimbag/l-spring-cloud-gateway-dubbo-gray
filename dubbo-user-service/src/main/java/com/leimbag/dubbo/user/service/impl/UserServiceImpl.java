package com.leimbag.dubbo.user.service.impl;

import com.leimbag.dubbo.user.service.UserService;
import com.leimbag.dubbo.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author leimbag
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    @Autowired
    private WalletService walletService;

    @Override
    public String getUserName(Long uid) {
        return "TestUser:" + uid;
    }

    @Override
    public String getWalletBalance(Long uid) {
        return walletService.getWallet(uid);
    }
}
