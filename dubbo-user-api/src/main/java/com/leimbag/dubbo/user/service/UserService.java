package com.leimbag.dubbo.user.service;

/**
 * @author leimbag
 */
public interface UserService {
    String getUserName(Long uid);

    String getWalletBalance(Long uid);
}
