package com.nadarzy.springcassandrabetterreadsdata.connection;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

/**
 * @author Julian Nadarzy on 04/10/2021
 */
@ConfigurationProperties(prefix = "datastax.astra")
public class DataStaxAstraProperties {

    private File secureConnectBundle;

    public File getSecureConnectBundle() {
        return secureConnectBundle;
    }

    public void setSecureConnectBundle(File secureConnectBundle) {
        this.secureConnectBundle = secureConnectBundle;
    }


}
