package com.ke.search;

import org.jdesktop.swingx.JXLabel;

import javax.swing.*;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/17 15:42
 * @Version 1.0
 * @Description
 */
public class AskListModel extends AbstractListModel<Object> {


    public AskListModel() {
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public Object getElementAt(int index) {
		return new JXLabel();
	}

	public void update() {
		fireContentsChanged(this, 0, getSize() - 1);
	}


	public void updateFromInput() {
		update();
	}

}
