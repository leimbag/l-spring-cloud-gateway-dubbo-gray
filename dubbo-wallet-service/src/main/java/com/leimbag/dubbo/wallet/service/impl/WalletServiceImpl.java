package com.leimbag.dubbo.wallet.service.impl;

import com.leimbag.dubbo.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author leimbag
 */
@Service("walletService")
public class WalletServiceImpl implements WalletService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getWallet(Long uid) {
//        try {
//            TimeUnit.SECONDS.sleep(10);
//        } catch (InterruptedException e) {
//            logger.error(e.getMessage(), e);
//        }
//        logger.info("等待10秒，uid={}", uid);
        logger.info("查询用户uid={}余额", uid);
        return "Wallet:" + uid;
    }

    @Override
    public String getWalletName(Long uid){
        logger.info("查询用户uid={}钱包名称", uid);
        return "Wallet Name:" + uid;
    }
}
