package com.lsr.repomentor.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoInfoVO {
    private Long repoId;
    private String repoName;
    private String localPath;
}
