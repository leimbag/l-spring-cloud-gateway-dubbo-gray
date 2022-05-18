package com.leimbag.dubbo.wallet.service;

/**
 * @author leimbag
 */
public interface WalletService {
    String getWallet(Long uid);

    String getWalletName(Long uid);
}
