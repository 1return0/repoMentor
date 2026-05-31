package com.lsr.repomentor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lsr.repomentor.dto.RepoImportDTO;
import com.lsr.repomentor.entity.RepoInfo;
import com.lsr.repomentor.vo.RepoInfoVO;

public interface RepoInfoService extends IService<RepoInfo> {
    RepoInfoVO importRepo(RepoImportDTO dto);
}
