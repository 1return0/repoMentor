package com.lsr.repomentor.service;

import com.lsr.repomentor.vo.RepoFileVO;

import java.util.List;

public interface RepoFileService {
    void scanRepoFiles(Long repoId,String localPath);

    List<RepoFileVO> listFilesByRepoId(Long repoId);
}
