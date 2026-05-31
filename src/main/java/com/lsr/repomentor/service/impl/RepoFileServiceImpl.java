package com.lsr.repomentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.lsr.repomentor.entity.RepoFile;
import com.lsr.repomentor.mapper.RepoFileMapper;
import com.lsr.repomentor.service.RepoFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;

@Service
@RequiredArgsConstructor
public class RepoFileServiceImpl implements RepoFileService {
    private final RepoFileMapper repoFileMapper;
    @Override
    public void scanRepoFiles(Long repoId, String localPath) {
        if(repoId == null){
            throw new RuntimeException("仓库ID不能为空");
        }
        if (!StringUtils.hasText(localPath)) {
            throw new RuntimeException("仓库本地路径不能为空");
        }
        File root = new File(localPath);
        if (!root.exists() || !root.isDirectory()) {
            throw new RuntimeException("仓库目录不存在：" + localPath);
        }
        LambdaQueryWrapper<RepoFile> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RepoFile::getRepoId, repoId);
        repoFileMapper.delete(deleteWrapper);
        scanDirectory(repoId, root, root);
    }
    private void scanDirectory(Long repoId, File root, File current){
        File[] files = current.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files){
            if(shouldIgnore(file)) continue;

            if (file.isDirectory()) {
                scanDirectory(repoId, root, file);
                continue;
            }

            String relativePath = root.toURI()
                    .relativize(file.toURI())
                    .getPath();
            RepoFile repoFile = RepoFile.builder()
                    .repoId(repoId)
                    .filePath(relativePath)
                    .fileName(file.getName())
                    .fileType(getFileType(file.getName()))
                    .fileSize(file.length())
                    .status(0)
                    .build();
            repoFileMapper.insert(repoFile);
        }
    }
    private boolean shouldIgnore(File file) {
        String name = file.getName();

        if (file.isDirectory()) {
            return name.equals(".git")
                    || name.equals(".idea")
                    || name.equals(".vscode")
                    || name.equals("target")
                    || name.equals("node_modules")
                    || name.equals("dist")
                    || name.equals("build")
                    || name.equals("out");
        }

        String lowerName = name.toLowerCase();

        return lowerName.endsWith(".jar")
                || lowerName.endsWith(".war")
                || lowerName.endsWith(".class")
                || lowerName.endsWith(".exe")
                || lowerName.endsWith(".dll")
                || lowerName.endsWith(".so")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".gif")
                || lowerName.endsWith(".ico")
                || lowerName.endsWith(".pdf")
                || lowerName.endsWith(".zip")
                || lowerName.endsWith(".rar")
                || lowerName.endsWith(".7z")
                || lowerName.endsWith(".log");
    }
    private String getFileType(String fileName){
        if (!StringUtils.hasText(fileName)) {
            return "unknown";
        }
        int index = fileName.lastIndexOf(".");
        if (index < 0 || index == fileName.length() - 1) {
            return "unknown";
        }
        String fileType = fileName.substring(index+1);
        return fileType.toLowerCase();
    }
}
