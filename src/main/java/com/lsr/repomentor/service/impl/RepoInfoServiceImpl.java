package com.lsr.repomentor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lsr.repomentor.dto.RepoImportDTO;
import com.lsr.repomentor.entity.RepoInfo;
import com.lsr.repomentor.mapper.RepoInfoMapper;
import com.lsr.repomentor.service.RepoInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
@Service
public class RepoInfoServiceImpl extends ServiceImpl<RepoInfoMapper, RepoInfo> implements RepoInfoService {
    @Override
    public Long importRepo(RepoImportDTO dto) {
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
            return existRepo.getId();
        }

        RepoInfo repoInfo = RepoInfo.builder()
                .userId(userId)
                .repoUrl(repoUrl)
                .branchName(branchName)
                .repoName(parseRepoName(repoUrl))
                .status(0)
                .build();

        this.save(repoInfo);
        return repoInfo.getId();
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
