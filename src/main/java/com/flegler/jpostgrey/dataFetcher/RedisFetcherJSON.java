package com.flegler.jpostgrey.dataFetcher;

public class RedisFetcherJSON {

    private Long firstConnect;
    private Long lastConnect;

    public Long getFirstConnect() {
        return firstConnect;
    }

    public void setFirstConnect(Long firstConnect) {
        this.firstConnect = firstConnect;
    }

    public Long getLastConnect() {
        return lastConnect;
    }

    public void setLastConnect(Long lastConnect) {
        this.lastConnect = lastConnect;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((firstConnect == null) ? 0 : firstConnect.hashCode());
        result = prime * result
                + ((lastConnect == null) ? 0 : lastConnect.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RedisFetcherJSON other = (RedisFetcherJSON) obj;
        if (firstConnect == null) {
            if (other.firstConnect != null)
                return false;
        } else if (!firstConnect.equals(other.firstConnect))
            return false;
        if (lastConnect == null) {
            if (other.lastConnect != null)
                return false;
        } else if (!lastConnect.equals(other.lastConnect))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RedisFetcherJSON [firstConnect=" + firstConnect
                + ", lastConnect=" + lastConnect + "]";
    }

}
