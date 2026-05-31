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
@TableName("repo_file")
public class RepoFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long repoId;
    private String filePath;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}