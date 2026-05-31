package com.lsr.repomentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lsr.repomentor.dto.RepoImportDTO;
import com.lsr.repomentor.entity.RepoInfo;
import com.lsr.repomentor.mapper.RepoInfoMapper;
import com.lsr.repomentor.service.GitHubCloneService;
import com.lsr.repomentor.service.RepoFileService;
import com.lsr.repomentor.service.RepoInfoService;
import com.lsr.repomentor.vo.RepoInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
@Service
@RequiredArgsConstructor
public class RepoInfoServiceImpl extends ServiceImpl<RepoInfoMapper, RepoInfo> implements RepoInfoService {
    private final GitHubCloneService gitHubCloneService;
    private final RepoFileService repoFileService;
    @Override
    public RepoInfoVO importRepo(RepoImportDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getRepoUrl())) {
            throw new RuntimeException("仓库地址不能为空");
        }

        Long userId = 0L; // 暂时写死，后面登录后从 Token 里取
        String repoUrl = dto.getRepoUrl();
        String branchName = StringUtils.hasText(dto.getBranchName()) ? dto.getBranchName() : "main";

        RepoInfo existRepo = this.lambdaQuery()
                .eq(RepoInfo::getUserId, userId)
                .eq(RepoInfo::getRepoUrl, repoUrl)
                .eq(RepoInfo::getBranchName, branchName)
                .one();

        if (existRepo != null) {
            return RepoInfoVO.builder()
                    .repoId(existRepo.getId())
                    .repoName(parseRepoName(repoUrl))
                    .localPath(existRepo.getLocalPath())
                    .build();
        }

        RepoInfo repoInfo = RepoInfo.builder()
                .userId(userId)
                .repoUrl(repoUrl)
                .branchName(branchName)
                .repoName(parseRepoName(repoUrl))
                .status(0)
                .build();

        this.save(repoInfo);
        String localPath = null;
        try {
            LambdaUpdateWrapper<RepoInfo> processingWrapper = new LambdaUpdateWrapper<>();
            processingWrapper.eq(RepoInfo::getId, repoInfo.getId());

            RepoInfo processingData = RepoInfo.builder()
                    .status(1)
                    .build();

            this.update(processingData, processingWrapper);

            localPath = gitHubCloneService.cloneRepo(repoUrl, branchName, repoInfo.getId());

            repoFileService.scanRepoFiles(repoInfo.getId(), localPath);

            LambdaUpdateWrapper<RepoInfo> successWrapper = new LambdaUpdateWrapper<>();
            successWrapper.eq(RepoInfo::getId, repoInfo.getId());

            RepoInfo successData = RepoInfo.builder()
                    .localPath(localPath)
                    .status(2)
                    .build();

            this.update(successData, successWrapper);

        } catch (Exception e) {
            LambdaUpdateWrapper<RepoInfo> failWrapper = new LambdaUpdateWrapper<>();
            failWrapper.eq(RepoInfo::getId, repoInfo.getId());

            RepoInfo failData = RepoInfo.builder()
                    .status(3)
                    .build();

            this.update(failData, failWrapper);

            throw new RuntimeException(e.getMessage());
        }
        return RepoInfoVO.builder()
                .repoId(repoInfo.getId())
                .repoName(parseRepoName(repoUrl))
                .localPath(localPath)
                .build();
    }
    private String parseRepoName(String repoUrl){
        String url = repoUrl;
        if(repoUrl.endsWith(".git")){
             url = repoUrl.substring(0,repoUrl.length()-4);
        }
        int index = url.lastIndexOf("/");
        return index>=0?url.substring(index+1):url;
    }
}
