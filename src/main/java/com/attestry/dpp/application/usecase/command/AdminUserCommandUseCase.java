package com.attestry.dpp.application.usecase.command;

public interface AdminUserCommandUseCase {
    void approveUser(String userId);
    void rejectUser(String userId);
}
