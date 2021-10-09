package com.leimbag.web.core.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author leimbag
 */
@ConfigurationProperties(prefix = "web.gray")
public class WebGrayProperties {
    private String forceUseTag;

    public String getForceUseTag() {
        return forceUseTag;
    }

    public void setForceUseTag(String forceUseTag) {
        this.forceUseTag = forceUseTag;
    }
}
