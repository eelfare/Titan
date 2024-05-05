package com.yunji.titanrtx.plugin.http.tools;

/**
 * @author Denim.leihz 2019-11-09 11:45 AM
 */
public class Header {

    private String key;

    private String value;

    public Header(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
