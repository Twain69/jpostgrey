package com.flegler.jpostgrey.dataFetcher;


public class FetcherResult {

    private Boolean whitelisted = false;
    private Long firstConnect;

    public Boolean getWhitelisted() {
        return whitelisted;
    }

    public void setWhitelisted(Boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    public Long getFirstConnect() {
        return firstConnect;
    }

    public void setFirstConnect(Long firstConnect) {
        this.firstConnect = firstConnect;
    }

}
