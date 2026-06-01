package com.lsr.repomentor.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoStatusVO {

    private Long repoId;

    private String repoName;

    private Integer status;

    private String statusText;

    private String localPath;
}