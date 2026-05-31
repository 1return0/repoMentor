package com.lsr.repomentor.service.impl;

import com.lsr.repomentor.config.RepoProperties;
import com.lsr.repomentor.service.GitHubCloneService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class GitHubCloneServiceImpl implements GitHubCloneService {

    private final RepoProperties repoProperties;

    public String cloneRepo(String repoUrl, String branchName, Long repoId) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {
            String localPath = repoProperties.getBasePath() + File.separator + repoId;
            File localDir = new File(localPath);

            if (localDir.exists()) {
                return localPath;
            }

            Future<String> future = executorService.submit(() -> {
                System.out.println("开始 clone：" + repoUrl);
                System.out.println("本地路径：" + localPath);

                try (Git git = Git.cloneRepository()
                        .setURI(repoUrl)
                        .setBranch(branchName)
                        .setDirectory(localDir)
                        .call()) {

                    System.out.println("clone 完成：" + localPath);
                    return localPath;
                }
            });

            return future.get(90, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            throw new RuntimeException("clone 仓库超时，请检查网络或仓库是否过大");

        } catch (Exception e) {
            throw new RuntimeException("clone 仓库失败：" + e.getMessage());

        } finally {
            executorService.shutdownNow();
        }
    }
}