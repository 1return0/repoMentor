package com.lsr.repomentor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lsr.repomentor.dto.RepoImportDTO;
import com.lsr.repomentor.entity.RepoInfo;

public interface RepoInfoService extends IService<RepoInfo> {
    Long importRepo(RepoImportDTO dto);
}
