package com.ke.setting.user.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/9/2 17:17
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangeFileTreeOption {

	// 分割面板上第一个组件所占界面的比例
	private float firstComponentProportion;

	// 上次关闭时是否显示diffPanel
	private boolean previousShowDiffPanel = true;
}
