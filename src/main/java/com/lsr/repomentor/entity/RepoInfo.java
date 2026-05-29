package com.lsr.repomentor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("repo_info")
@Builder
public class RepoInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String repoName;
    private Long userId;
    private String repoUrl;

    private String branchName;

    private String localPath;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}