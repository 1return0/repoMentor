package com.lsr.repomentor.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkSearchVO {

    private Long chunkId;

    private Long repoId;

    private String repoName;

    private Long fileId;

    private String filePath;

    private String fileName;

    private String fileType;

    private Integer chunkIndex;

    private Integer startLine;

    private Integer endLine;

    private String content;
}