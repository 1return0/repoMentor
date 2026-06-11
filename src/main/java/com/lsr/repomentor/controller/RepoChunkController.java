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
        return Result.ok(repoChunkService.searchChunks(dto));
    }
    @GetMapping("/file")
    public Result<List<ChunkSearchVO>> listChunksByFilePath(
            @RequestParam Long repoId,
            @RequestParam String filePath
    ) {
        return Result.ok(repoChunkService.listChunksByFilePath(repoId, filePath));
    }
    @GetMapping("/list")
    public Result<List<ChunkSearchVO>> listChunks(@RequestParam Long repoId){
        return Result.ok(repoChunkService.listChunksByRepoId(repoId));
    }
}