package com.attestry.dpp.application.port;

public interface FileReadUrlPort {
    String createPresignedReadUrl(String rawUrlOrObjectPath);
}
