package com.lsr.repomentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lsr.repomentor.entity.RepoChunk;
import com.lsr.repomentor.entity.RepoFile;
import com.lsr.repomentor.entity.RepoInfo;
import com.lsr.repomentor.mapper.RepoChunkMapper;
import com.lsr.repomentor.mapper.RepoFileMapper;
import com.lsr.repomentor.mapper.RepoInfoMapper;
import com.lsr.repomentor.service.RepoChunkService;
import com.lsr.repomentor.service.RepoInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepoChunkServiceImpl implements RepoChunkService {
    private final RepoChunkMapper repoChunkMapper;
    private final RepoInfoMapper repoInfoMapper;
    private final RepoFileMapper repoFileMapper;
    @Value("${chunkLines.max}")
    private int maxChunkLines;
    public void buildChunks(Long repoId){
        if(repoId == null){
            throw new RuntimeException("仓库ID不能为空");
        }
        //仓库目录路径
        LambdaQueryWrapper<RepoInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(RepoInfo::getLocalPath)
                .eq(RepoInfo::getId,repoId);
        RepoInfo repoInfo = repoInfoMapper.selectOne(wrapper);

        if (repoInfo == null || !StringUtils.hasText(repoInfo.getLocalPath())) {
            throw new RuntimeException("仓库不存在或本地路径为空");
        }
        //
        LambdaQueryWrapper<RepoChunk> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RepoChunk::getRepoId, repoId);
        repoChunkMapper.delete(deleteWrapper);
        //该仓库下所有文件相对路径
        LambdaQueryWrapper<RepoFile> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(RepoFile::getRepoId, repoId);
        List<RepoFile> fileList = repoFileMapper.selectList(fileWrapper);
        //切块存库
        for (RepoFile repoFile : fileList){
            if (!isSupportedFile(repoFile.getFileType())) {
                continue;
            }

            buildFileChunks(repoInfo.getLocalPath(), repoFile);
            //切块后更新状态
            LambdaUpdateWrapper<RepoFile> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(RepoFile::getId, repoFile.getId());

            RepoFile updateData = RepoFile.builder()
                    .status(1)
                    .build();

            repoFileMapper.update(updateData, updateWrapper);
        }
    }
    private void buildFileChunks(String localPath, RepoFile repoFile) {
        try {
            File file = new File(localPath, repoFile.getFilePath());

            if (!file.exists() || !file.isFile()) {
                return;
            }
            //每个元素是一行数据，file.toPath()也是文件File的新写法
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            if (lines.isEmpty()) {
                return;
            }

            int chunkIndex = 0;

            for (int start = 0; start < lines.size(); start += maxChunkLines) {
                int end = Math.min(start + maxChunkLines, lines.size());

                String content = String.join("\n", lines.subList(start, end));

                if (!StringUtils.hasText(content)) {
                    continue;
                }

                RepoChunk repoChunk = RepoChunk.builder()
                        .repoId(repoFile.getRepoId())
                        .fileId(repoFile.getId())
                        .chunkIndex(chunkIndex++)
                        .content(content)
                        .startLine(start + 1)
                        .endLine(end)
                        .embeddingStatus(0)
                        .build();

                repoChunkMapper.insert(repoChunk);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件切块失败：" + repoFile.getFilePath(), e);
        }
    }
    private boolean isSupportedFile(String fileType) {
        if (!StringUtils.hasText(fileType)) {
            return false;
        }

        return fileType.equals("java")
                || fileType.equals("xml")
                || fileType.equals("yml")
                || fileType.equals("yaml")
                || fileType.equals("properties")
                || fileType.equals("md")
                || fileType.equals("txt")
                || fileType.equals("html")
                || fileType.equals("css")
                || fileType.equals("scss")
                || fileType.equals("js")
                || fileType.equals("ts")
                || fileType.equals("json")
                || fileType.equals("gradle");
    }
}
