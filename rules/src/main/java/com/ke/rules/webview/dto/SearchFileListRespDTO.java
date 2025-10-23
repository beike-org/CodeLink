package com.ke.rules.webview.dto;

import com.ke.webview.dto.FileContentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchFileListRespDTO {

	private String uuid;

	private List<FileContentDTO> fileContentDTOList;
}
