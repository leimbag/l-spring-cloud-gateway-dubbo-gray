package com.leimbag.gateway.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * @author leimbag
 */
@ConfigurationProperties(prefix = "gateway.request.log")
public class GatewayRequestLogProperties {

    /**
     * 保存原始RequestHeader信息
     */
    private Boolean saveRequestHeader = false;
    /**
     * 保留原始响应头信息
     */
    private Boolean saveResponseHeader = false;
    /**
     * 保存Post的RequestBody体信息
     */
    private Boolean savePostRequestBody = false;

    /**
     * 保存Get的RequestBody体信息
     */
    private Boolean saveGetRequestBody = false;

    /**
     * 解码RequestBody信息
     * URL.decode + Base64.decode
     *
     * 一般用于查看前端原始信息
     */
    private Boolean decodeRequestBody = false;

    /**
     * 保存响应ResponseBody体信息
     */
    private Boolean saveResponseBody = false;

    /**
     * 忽略路径
     */
    private Set<String> ignorePaths = new HashSet<>();

    /**
     * 保存UserOpenId，便于追踪
     */
    private Boolean saveUserOpenId = false;

    public Boolean getSavePostRequestBody() {
        return savePostRequestBody;
    }

    public void setSavePostRequestBody(Boolean savePostRequestBody) {
        this.savePostRequestBody = savePostRequestBody;
    }

    public Boolean getDecodeRequestBody() {
        return decodeRequestBody;
    }

    public void setDecodeRequestBody(Boolean decodeRequestBody) {
        this.decodeRequestBody = decodeRequestBody;
    }

    public Boolean getSaveUserOpenId() {
        return saveUserOpenId;
    }

    public void setSaveUserOpenId(Boolean saveUserOpenId) {
        this.saveUserOpenId = saveUserOpenId;
    }

    public Boolean getSaveGetRequestBody() {
        return saveGetRequestBody;
    }

    public void setSaveGetRequestBody(Boolean saveGetRequestBody) {
        this.saveGetRequestBody = saveGetRequestBody;
    }

    public Set<String> getIgnorePaths() {
        return ignorePaths;
    }

    public void setIgnorePaths(Set<String> ignorePaths) {
        this.ignorePaths = ignorePaths;
    }

    public Boolean getSaveResponseBody() {
        return saveResponseBody;
    }

    public void setSaveResponseBody(Boolean saveResponseBody) {
        this.saveResponseBody = saveResponseBody;
    }

    public Boolean getSaveRequestHeader() {
        return saveRequestHeader;
    }

    public void setSaveRequestHeader(Boolean saveRequestHeader) {
        this.saveRequestHeader = saveRequestHeader;
    }

    public Boolean getSaveResponseHeader() {
        return saveResponseHeader;
    }

    public void setSaveResponseHeader(Boolean saveResponseHeader) {
        this.saveResponseHeader = saveResponseHeader;
    }
}
