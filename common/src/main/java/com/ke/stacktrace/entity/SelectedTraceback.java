package com.ke.stacktrace.entity;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectedTraceback {
	private String traceback;
	private Integer lineFrom;
	private Integer lineTo;
}
