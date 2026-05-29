package com.lsr.repomentor.service;

public interface GitHubCloneService {
    String cloneRepo(String repoUrl, String branchName, Long repoId);
}
