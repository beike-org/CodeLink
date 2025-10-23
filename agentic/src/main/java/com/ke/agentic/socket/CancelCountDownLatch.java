package com.ke.agentic.socket;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/12 11:11
 * @Description
 */
@Getter
public class CancelCountDownLatch extends CountDownLatch {

	@Setter
	private boolean isCancel = false;

	/**
	 * Constructs a {@code CountDownLatch} initialized with the given count.
	 *
	 * @param count the number of times {@link #countDown} must be invoked
	 *              before threads can pass through {@link #await}
	 * @throws IllegalArgumentException if {@code count} is negative
	 */
	public CancelCountDownLatch(int count) {
		super(count);
	}


}
