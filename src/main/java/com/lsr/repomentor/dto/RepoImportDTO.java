package com.lsr.repomentor.dto;

import lombok.Data;

@Data
public class RepoImportDTO {

    private String repoUrl;

    private String branchName;
}