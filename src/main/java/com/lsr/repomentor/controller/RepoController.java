package com.lsr.repomentor.controller;

import com.lsr.repomentor.common.Result;
import com.lsr.repomentor.dto.RepoImportDTO;
import com.lsr.repomentor.entity.RepoInfo;
import com.lsr.repomentor.service.RepoInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repo")
@RequiredArgsConstructor
public class RepoController {

    private final RepoInfoService repoInfoService;

    @PostMapping("/import")
    public Result<Long> importRepo(@RequestBody RepoImportDTO dto) {
        try{
            return Result.ok(repoInfoService.importRepo(dto));
        } catch (Exception e){
            return Result.fail("添加仓库失败：" + e.getMessage());
        }

    }

    @GetMapping("/{repoId}")
    public Result<RepoInfo> getRepo(@PathVariable Long repoId) {
        try{
            return Result.ok(repoInfoService.getById(repoId));
        }catch (Exception e){
            return Result.fail("查询失败：" + e.getMessage());
        }

    }
}