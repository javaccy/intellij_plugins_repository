package com.javaccy.intellij.repository;

import java.math.BigDecimal;

public class Plugin implements Comparable<Plugin> {

    private String id;
    private String url;
    private Double version;
    private Integer bigVersion;


    public Plugin(String id,String url, Double version, Integer bigVersion) {
        this.id = id;
        this.url = url;
        this.version = version;
        this.bigVersion = bigVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public Integer getBigVersion() {
        return bigVersion;
    }


    @Override
    public int compareTo(Plugin o) {
        return BigDecimal.valueOf(o.getVersion()).compareTo(BigDecimal.valueOf(this.getVersion()));
    }
}
