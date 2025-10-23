package com.ke.utils;

import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/5 18:35
 * @Description
 */
public class ProcessUtil {
    private static final Logger LOG = Logger.getInstance(ProcessUtil.class);


    public static void waitForProcessTermination(@NotNull KillableProcessHandler process, @NotNull Duration duration, @NotNull Duration interval) {
        long startTime = System.currentTimeMillis();
        LOG.debug(String.format("Waiting for process termination (max %s, interval %s)", duration, interval));

        while(true) {
            if (process.isProcessTerminated()) {
                LOG.debug("Process terminated");
                break;
            }

            if (System.currentTimeMillis() - startTime > duration.toMillis()) {
                LOG.warn("Process did not terminate in time, destroying it");
                process.destroyProcess();
                break;
            }

            try {
                Thread.sleep(interval.toMillis());
            } catch (InterruptedException interruptedException) {
                LOG.error("Interrupted when waiting for process to terminate", interruptedException);
                Thread.currentThread().interrupt();
                break;
            }
        }

    }
}
