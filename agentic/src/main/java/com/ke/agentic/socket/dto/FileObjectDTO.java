package com.ke.agentic.socket.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件对象DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileObjectDTO {

    @JSONField(name = "file_path")
    private String filePath;

    @JSONField(name = "file_content")
    private String fileContent;

    @JSONField(name = "file_operation")
    private FileOperationType fileOperation;
}