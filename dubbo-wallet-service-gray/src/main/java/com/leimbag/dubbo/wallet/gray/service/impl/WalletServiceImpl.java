package com.leimbag.dubbo.wallet.gray.service.impl;

import com.leimbag.dubbo.wallet.service.WalletService;
import org.springframework.stereotype.Service;

/**
 * @author leimbag
 */
@Service("walletService")
public class WalletServiceImpl implements WalletService {
    public String getWallet(Long uid) {
        return "Gray Wallet:" + uid;
    }
}
