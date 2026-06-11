package com.lsr.repomentor.service;

import com.lsr.repomentor.dto.ChunkSearchDTO;
import com.lsr.repomentor.vo.ChunkSearchVO;

import java.util.List;

public interface RepoChunkService {
    void buildChunks(Long repoId);
    List<ChunkSearchVO> searchChunks(ChunkSearchDTO dto);

    List<ChunkSearchVO> listChunksByFilePath(Long repoId, String filePath);

    List<ChunkSearchVO> listChunksByRepoId(Long repoId);
}
