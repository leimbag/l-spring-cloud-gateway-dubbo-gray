package com.leimbag.dubbo.wallet.service.impl;

import com.leimbag.dubbo.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author leimbag
 */
@Service("walletService")
public class WalletServiceImpl implements WalletService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public String getWallet(Long uid) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("等待10秒，uid={}", uid);
        return "Wallet:" + uid;
    }
}
