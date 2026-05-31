package com.lsr.repomentor.dto;

import lombok.Data;

@Data
public class ChunkSearchDTO {

    private Long repoId;

    private String keyword;

    /**
     * 返回数量，默认 10，最大建议 50
     */
    private Integer topK;
}