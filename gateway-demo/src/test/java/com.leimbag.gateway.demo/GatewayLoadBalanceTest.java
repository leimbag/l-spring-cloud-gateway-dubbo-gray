package com.leimbag.gateway.demo;

import com.leimbag.demo.core.util.HttpUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author leimbag
 */
public class GatewayLoadBalanceTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testLoadBalanceGet() {
        String url = "http://localhost:19840/api/user/test?name=asdf";
        for (int i = 0; i < 2000; i++) {
            try {
                String result = HttpUtil.get(url);
                logger.info("第{}次result:{}", i, result);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Test
    public void testLoadBalancePost() {
        String url = "http://localhost:19840/api/user/test";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "sdfa");
        int errorCount = 0;
        for (int i = 0; i < 1000; i++) {
            try {
                String result = HttpUtil.postUrlEncodedForm(url, paramMap);
                logger.info("第{}次result:{}", i, result);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                errorCount++;
            }
        }
        logger.info("测试完毕，errorCount：{}", errorCount);
    }

    @Test
    public void testBeforeClose() {
        String url = "http://localhost:19840/api/user/wait";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("name", "sdfa");
        int errorCount = 0;
        for (int i = 0; i < 20; i++) {
            try {
                String result = HttpUtil.postUrlEncodedForm(url, paramMap);
                logger.info("第{}次result:{}", i, result);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                errorCount++;
            }
        }
        logger.info("测试完毕，errorCount：{}", errorCount);
    }
}
