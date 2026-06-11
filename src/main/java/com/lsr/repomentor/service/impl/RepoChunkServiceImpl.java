package com.lsr.repomentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lsr.repomentor.common.Result;
import com.lsr.repomentor.dto.ChunkSearchDTO;
import com.lsr.repomentor.entity.RepoChunk;
import com.lsr.repomentor.entity.RepoFile;
import com.lsr.repomentor.entity.RepoInfo;
import com.lsr.repomentor.mapper.RepoChunkMapper;
import com.lsr.repomentor.mapper.RepoFileMapper;
import com.lsr.repomentor.mapper.RepoInfoMapper;
import com.lsr.repomentor.service.RepoChunkService;
import com.lsr.repomentor.service.RepoInfoService;
import com.lsr.repomentor.vo.ChunkSearchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Override
    public List<ChunkSearchVO> searchChunks(ChunkSearchDTO dto) {
        if (dto == null || dto.getRepoId() == null) {
            throw new RuntimeException("仓库ID不能为空");
        }
        if (!StringUtils.hasText(dto.getKeyword())) {
            throw new RuntimeException("关键词不能为空");
        }

        Long repoId = dto.getRepoId();
        String keyword = dto.getKeyword().trim();

        int topK = dto.getTopK() == null ? 10 : dto.getTopK();
        topK = Math.max(1, Math.min(topK, 50));

        RepoInfo repoInfo = repoInfoMapper.selectById(repoId);
        if (repoInfo == null) {
            throw new RuntimeException("仓库不存在");
        }

        /*
         * 先查文件表：
         * 支持 filePath、fileName、fileType 匹配。
         * 比如 keyword = Controller，可以命中文件名 RestController.java。
         */
        LambdaQueryWrapper<RepoFile> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(RepoFile::getRepoId, repoId)
                .and(wrapper -> wrapper
                        .like(RepoFile::getFilePath, keyword)
                        .or()
                        .like(RepoFile::getFileName, keyword)
                        .or()
                        .like(RepoFile::getFileType, keyword)
                );

        List<RepoFile> matchedFiles = repoFileMapper.selectList(fileWrapper);

        List<Long> matchedFileIds = matchedFiles.stream()
                .map(RepoFile::getId)
                .toList();

        /*
         * 再查 chunk 表：
         * 1. content LIKE keyword
         * 2. 或者 fileId 在匹配到的文件列表中
         */
        LambdaQueryWrapper<RepoChunk> chunkWrapper = new LambdaQueryWrapper<>();
        chunkWrapper.eq(RepoChunk::getRepoId, repoId)
                .and(wrapper -> {
                    wrapper.like(RepoChunk::getContent, keyword);

                    if (!matchedFileIds.isEmpty()) {
                        wrapper.or().in(RepoChunk::getFileId, matchedFileIds);
                    }
                })
                .orderByAsc(RepoChunk::getFileId)
                .orderByAsc(RepoChunk::getChunkIndex)
                .last("LIMIT " + topK);

        List<RepoChunk> chunkList = repoChunkMapper.selectList(chunkWrapper);

        if (chunkList.isEmpty()) {
            return Collections.emptyList();
        }

        /*
         * 避免 N+1 查询：
         * 之前你是每个 chunk 查一次 repo_file。
         * 现在改成一次性批量查。
         */
        Set<Long> fileIds = chunkList.stream()
                .map(RepoChunk::getFileId)
                .collect(Collectors.toSet());

        Map<Long, RepoFile> fileMap = repoFileMapper.selectBatchIds(fileIds)
                .stream()
                .collect(Collectors.toMap(RepoFile::getId, file -> file));

        return chunkList.stream()
                .map(chunk -> {
                    RepoFile repoFile = fileMap.get(chunk.getFileId());

                    return ChunkSearchVO.builder()
                            .chunkId(chunk.getId())
                            .repoId(chunk.getRepoId())
                            .repoName(repoInfo.getRepoName())
                            .fileId(chunk.getFileId())
                            .filePath(repoFile == null ? null : repoFile.getFilePath())
                            .fileName(repoFile == null ? null : repoFile.getFileName())
                            .fileType(repoFile == null ? null : repoFile.getFileType())
                            .chunkIndex(chunk.getChunkIndex())
                            .startLine(chunk.getStartLine())
                            .endLine(chunk.getEndLine())
                            .content(chunk.getContent())
                            .build();
                })
                .toList();
    }

    @Override
    public List<ChunkSearchVO> listChunksByFilePath(Long repoId, String filePath) {
        if (repoId==null) throw new RuntimeException("仓库ID不能为空");
        RepoInfo repoInfo = repoInfoMapper.selectById(repoId);
        if (repoInfo == null) {
            throw new RuntimeException("仓库不存在");
        }
        LambdaQueryWrapper<RepoFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RepoFile::getFilePath,filePath)
                .eq(RepoFile::getRepoId,repoId);
        RepoFile repoFile = repoFileMapper.selectOne(wrapper);
        if(repoFile==null){
            throw new RuntimeException("文件路径不存在");
        }

        Long id = repoFile.getId();
        LambdaQueryWrapper<RepoChunk> chunkWrapper = new LambdaQueryWrapper<>();
        chunkWrapper.eq(RepoChunk::getFileId,id)
                .orderByAsc(RepoChunk::getChunkIndex);
        List<RepoChunk> repoChunks = repoChunkMapper.selectList(chunkWrapper);
        return repoChunks.stream().map(repoChunk -> {
            return ChunkSearchVO.builder()
                    .repoId(repoInfo.getId())
                    .chunkId(repoChunk.getId())
                    .repoName(repoInfo.getRepoName())
                    .fileId(repoFile.getId())
                    .filePath(repoFile.getFilePath())
                    .fileType(repoFile.getFileType())
                    .fileName(repoFile.getFileName())
                    .chunkIndex(repoChunk.getChunkIndex())
                    .startLine(repoChunk.getStartLine())
                    .endLine(repoChunk.getEndLine())
                    .content(repoChunk.getContent())
                    .build();
        }).toList();
    }

    @Override
    public List<ChunkSearchVO> listChunksByRepoId(Long repoId) {
        if(repoId==null) throw new RuntimeException("repoId is empty");
        RepoInfo repoInfo = repoInfoMapper.selectOne(new LambdaQueryWrapper<RepoInfo>()
                .eq(RepoInfo::getId, repoId));
        if(repoInfo==null) throw new RuntimeException("repo is not exist");

        LambdaQueryWrapper<RepoChunk> chunkWrapper = new LambdaQueryWrapper<>();
        chunkWrapper.eq(RepoChunk::getRepoId,repoId)
                .orderByAsc(RepoChunk::getFileId)
                .orderByAsc(RepoChunk::getChunkIndex);
        List<RepoChunk> repoChunks = repoChunkMapper.selectList(chunkWrapper);

        if (repoChunks == null || repoChunks.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> fileIds = repoChunks.stream()
                .map(RepoChunk::getFileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (fileIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<RepoFile> repoFiles = repoFileMapper.selectBatchIds(fileIds);

        Map<Long, RepoFile> fileMap = repoFiles.stream()
                .collect(Collectors.toMap(
                        RepoFile::getId,
                        Function.identity()
                ));

        return repoChunks.stream()
                .map(repoChunk -> {
                    RepoFile repoFile = fileMap.get(repoChunk.getFileId());
                    if (repoFile == null) {
                        return null;
                    }

                    return ChunkSearchVO.builder()
                            .repoId(repoInfo.getId())
                            .repoName(repoInfo.getRepoName())
                            .chunkId(repoChunk.getId())
                            .fileId(repoFile.getId())
                            .filePath(repoFile.getFilePath())
                            .fileType(repoFile.getFileType())
                            .fileName(repoFile.getFileName())
                            .chunkIndex(repoChunk.getChunkIndex())
                            .startLine(repoChunk.getStartLine())
                            .endLine(repoChunk.getEndLine())
                            .content(repoChunk.getContent())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
//        Map<Long, List<RepoChunk>> collect = repoChunks.stream().collect(Collectors.groupingBy(RepoChunk::getFileId));
//
//        List<ChunkSearchVO> res = new ArrayList<>();
//        for(Map.Entry<Long,List<RepoChunk>> entry:collect.entrySet()){
//            Long fileId = entry.getKey();
//            List<RepoChunk> value = entry.getValue();
//            RepoFile repoFile = repoFileMapper.selectById(fileId);
//            if (repoFile == null) {
//                continue;
//            }
//            for(RepoChunk repoChunk:value){
//                res.add(ChunkSearchVO.builder()
//                        .repoId(repoInfo.getId())
//                        .chunkId(repoChunk.getId())
//                        .repoName(repoInfo.getRepoName())
//                        .fileId(repoFile.getId())
//                        .filePath(repoFile.getFilePath())
//                        .fileType(repoFile.getFileType())
//                        .fileName(repoFile.getFileName())
//                        .chunkIndex(repoChunk.getChunkIndex())
//                        .startLine(repoChunk.getStartLine())
//                        .endLine(repoChunk.getEndLine())
//                        .content(repoChunk.getContent())
//                        .build());
//            }
//        }
//        return res;

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
