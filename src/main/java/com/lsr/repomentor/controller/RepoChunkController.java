package com.lsr.repomentor.controller;

import com.lsr.repomentor.common.Result;
import com.lsr.repomentor.dto.ChunkSearchDTO;
import com.lsr.repomentor.service.RepoChunkService;
import com.lsr.repomentor.vo.ChunkSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chunk")
@RequiredArgsConstructor
public class RepoChunkController {

    private final RepoChunkService repoChunkService;

    @PostMapping("/search")
    public Result<List<ChunkSearchVO>> searchChunks(@RequestBody ChunkSearchDTO dto) {
        try {
            return Result.ok(repoChunkService.searchChunks(dto));
        } catch (Exception e) {
            return Result.fail("检索代码片段失败：" + e.getMessage());
        }
    }
}