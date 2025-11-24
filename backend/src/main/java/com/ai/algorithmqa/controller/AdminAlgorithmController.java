package com.ai.algorithmqa.controller;

import com.ai.algorithmqa.common.ApiResponse;
import com.ai.algorithmqa.domain.dto.AlgorithmCreateRequest;
import com.ai.algorithmqa.domain.dto.AlgorithmUpdateRequest;
import com.ai.algorithmqa.domain.entity.AlgorithmDetail;
import com.ai.algorithmqa.domain.entity.KnowledgeTopic;
import com.ai.algorithmqa.mapper.AlgorithmDetailMapper;
import com.ai.algorithmqa.mapper.KnowledgeTopicMapper;
import com.ai.algorithmqa.service.FileStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin/algorithms")
@RequiredArgsConstructor
public class AdminAlgorithmController {

    private final AlgorithmDetailMapper algorithmDetailMapper;
    private final KnowledgeTopicMapper topicMapper;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ApiResponse<List<AlgorithmDetail>> listAll() {
        return ApiResponse.ok(algorithmDetailMapper.selectList(null));
    }

    @GetMapping("/{id}")
    public ApiResponse<AlgorithmDetail> getById(@PathVariable Long id) {
        AlgorithmDetail detail = algorithmDetailMapper.selectById(id);
        if (detail == null) {
            return ApiResponse.error("算法不存在");
        }
        return ApiResponse.ok(detail);
    }

    @PostMapping
    public ApiResponse<AlgorithmDetail> create(@RequestBody AlgorithmCreateRequest request) {
        AlgorithmDetail detail = new AlgorithmDetail();
        detail.setTopicId(request.topicId());
        detail.setName(request.name());
        detail.setCoreIdea(request.coreIdea());
        detail.setStepBreakdown(request.stepBreakdown());
        detail.setTimeComplexity(request.timeComplexity());
        detail.setSpaceComplexity(request.spaceComplexity());
        detail.setCodeSnippet(request.codeSnippet());
        detail.setVisualizationHint(request.visualizationHint());
        detail.setMermaidCode(request.mermaidCode());

        algorithmDetailMapper.insert(detail);
        return ApiResponse.ok(detail);
    }

    @PutMapping("/{id}")
    public ApiResponse<AlgorithmDetail> update(@PathVariable Long id, @RequestBody AlgorithmUpdateRequest request) {
        AlgorithmDetail detail = algorithmDetailMapper.selectById(id);
        if (detail == null) {
            return ApiResponse.error("算法不存在");
        }

        if (request.name() != null)
            detail.setName(request.name());
        if (request.coreIdea() != null)
            detail.setCoreIdea(request.coreIdea());
        if (request.stepBreakdown() != null)
            detail.setStepBreakdown(request.stepBreakdown());
        if (request.timeComplexity() != null)
            detail.setTimeComplexity(request.timeComplexity());
        if (request.spaceComplexity() != null)
            detail.setSpaceComplexity(request.spaceComplexity());
        if (request.codeSnippet() != null)
            detail.setCodeSnippet(request.codeSnippet());
        if (request.visualizationHint() != null)
            detail.setVisualizationHint(request.visualizationHint());
        if (request.mermaidCode() != null)
            detail.setMermaidCode(request.mermaidCode());
        if (request.animationUrl() != null)
            detail.setAnimationUrl(request.animationUrl());

        algorithmDetailMapper.updateById(detail);
        return ApiResponse.ok(detail);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AlgorithmDetail detail = algorithmDetailMapper.selectById(id);
        if (detail == null) {
            return ApiResponse.error("算法不存在");
        }

        // 删除关联的动画文件
        if (detail.getAnimationUrl() != null && !detail.getAnimationUrl().isEmpty()) {
            try {
                String objectName = extractObjectNameFromUrl(detail.getAnimationUrl());
                fileStorageService.deleteFile(objectName);
            } catch (Exception e) {
                log.error("删除动画文件失败", e);
            }
        }

        algorithmDetailMapper.deleteById(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/animation")
    public ApiResponse<String> uploadAnimation(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        AlgorithmDetail detail = algorithmDetailMapper.selectById(id);
        if (detail == null) {
            return ApiResponse.error("算法不存在");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String objectName = "animations/" + UUID.randomUUID() + extension;

            // 删除旧文件
            if (detail.getAnimationUrl() != null && !detail.getAnimationUrl().isEmpty()) {
                try {
                    String oldObjectName = extractObjectNameFromUrl(detail.getAnimationUrl());
                    fileStorageService.deleteFile(oldObjectName);
                } catch (Exception e) {
                    log.warn("删除旧动画文件失败", e);
                }
            }

            // 上传新文件
            String url = fileStorageService.uploadFile(file, objectName);

            // 更新数据库
            detail.setAnimationUrl(url);
            int rows = algorithmDetailMapper.updateById(detail);
            log.info("更新算法动画 URL: id={}, url={}, rows={}", id, url, rows);

            return ApiResponse.ok(url);
        } catch (Exception e) {
            log.error("上传动画失败", e);
            return ApiResponse.error("上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/topics")
    public ApiResponse<List<KnowledgeTopic>> listTopics() {
        return ApiResponse.ok(topicMapper.selectList(null));
    }

    private String extractObjectNameFromUrl(String url) {
        // 从 URL 中提取对象名称
        // 例如: http://localhost:9000/algorithm-animations/animations/xxx.mp4 ->
        // animations/xxx.mp4
        int lastSlashIndex = url.indexOf("/", url.indexOf("//") + 2);
        if (lastSlashIndex != -1) {
            int bucketEndIndex = url.indexOf("/", lastSlashIndex + 1);
            if (bucketEndIndex != -1) {
                return url.substring(bucketEndIndex + 1);
            }
        }
        return url;
    }
}
