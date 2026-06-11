package com.lsr.repomentor.controller;

import com.lsr.repomentor.common.Result;
import com.lsr.repomentor.dto.RepoImportDTO;
import com.lsr.repomentor.entity.RepoInfo;
import com.lsr.repomentor.service.RepoFileService;
import com.lsr.repomentor.service.RepoInfoService;
import com.lsr.repomentor.vo.RepoFileVO;
import com.lsr.repomentor.vo.RepoInfoVO;
import com.lsr.repomentor.vo.RepoStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repo")
@RequiredArgsConstructor
public class RepoController {

    private final RepoInfoService repoInfoService;
    private final RepoFileService repoFileService;
    @PostMapping("/import")
    public Result<RepoInfoVO> importRepo(@RequestBody RepoImportDTO dto) {
        return Result.ok(repoInfoService.importRepo(dto));
    }

    @GetMapping("/{repoId}")
    public Result<RepoInfo> getRepo(@PathVariable Long repoId) {
        return Result.ok(repoInfoService.getById(repoId));
    }

    @GetMapping("/{repoId}/status")
    public Result<RepoStatusVO> getRepoStatus(@PathVariable Long repoId) {
        return Result.ok(repoInfoService.getRepoStatus(repoId));
    }
    @GetMapping("/files/{repoId}")
    public Result<List<RepoFileVO>> listRepoFiles(@PathVariable Long repoId) {
        return Result.ok(repoFileService.listFilesByRepoId(repoId));
    }
}