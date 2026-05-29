package com.lsr.repomentor.service.impl;

import com.lsr.repomentor.config.RepoProperties;
import com.lsr.repomentor.service.GitHubCloneService;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class GitHubCloneServiceImpl implements GitHubCloneService{
    private final RepoProperties repoProperties;
    public String cloneRepo(String repoUrl,String branchName,Long repoId){
        try {
            String localPath = repoProperties.getBasePath()+ File.separator+repoId;
            File localDir = new File(localPath);
            if(localDir.exists()){
                return localPath;
            }
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setBranch(branchName)
                    .setDirectory(localDir)
                    .call();
            return localPath;
        }catch (Exception e){
            throw new RuntimeException("clone 仓库失败：" + e.getMessage());
        }

    }
}
