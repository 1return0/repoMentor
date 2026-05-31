package com.lsr.repomentor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("repo_chunk")
public class RepoChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long repoId;
    private Long fileId;
    private Integer chunkIndex;
    private String content;
    private Integer startLine;
    private Integer endLine;
    private Integer embeddingStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}