package com.ke;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/7/7 18:01
 * @Description 轮询重试模板
 */
@RequiredArgsConstructor
public class RetryTemplate {
    private final int maxAttempts;
    private final long waitDuration;
    private final Class<? extends Exception> retryableException;
    private final Consumer<RetryContext> retryListener;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static Builder builder() {
        return new Builder();
    }

    public CompletableFuture<Boolean> execute(CheckedFunction<RetryContext, Boolean> operation) {
        return executeAttempt(operation, new RetryContext(), 1);
    }

    private CompletableFuture<Boolean> executeAttempt(CheckedFunction<RetryContext, Boolean> operation,
                                                      RetryContext context, int attempt) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (attempt > maxAttempts) {
            scheduler.shutdown();
            future.complete(false);
            return future;
        }

        context.setRetryCount(attempt);
        try {
            boolean result = operation.apply(context);
            if (result) {
                scheduler.shutdown();
                future.complete(true);
            } else {
                scheduleNextAttempt(operation, context, attempt, future);
            }
        } catch (Exception e) {
            if (!retryableException.isInstance(e) || attempt == maxAttempts) {
                scheduler.shutdown();
                future.completeExceptionally(e);
            } else {
                retryListener.accept(context);
                scheduleNextAttempt(operation, context, attempt, future);
            }
        }

        return future;
    }

    private void scheduleNextAttempt(CheckedFunction<RetryContext, Boolean> operation,
                                     RetryContext context,
                                     int attempt,
                                     CompletableFuture<Boolean> future) {
        if (attempt < maxAttempts) {
            scheduler.schedule(
                    () -> executeAttempt(operation, context, attempt + 1)
                            .thenAccept(future::complete)
                            .exceptionally(ex -> {
                                future.completeExceptionally(ex);
                                return null;
                            }),
                    waitDuration,
                    TimeUnit.MILLISECONDS
            );
        } else {
            scheduler.shutdown();
            future.complete(false);
        }
    }

    @lombok.Getter
    @lombok.Setter
    public static class RetryContext {
        private int retryCount;
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    public static class Builder {
        private int maxAttempts = 1;
        private long waitDuration = 0;
        private Class<? extends Exception> retryableException = Exception.class;
        private Consumer<RetryContext> retryListener = context -> {
        };

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder waitDuration(Duration duration) {
            this.waitDuration = duration.toMillis();
            return this;
        }

        public Builder retryOn(Class<? extends Exception> exception) {
            this.retryableException = exception;
            return this;
        }

        public Builder onRetry(Consumer<RetryContext> listener) {
            this.retryListener = listener;
            return this;
        }

        public RetryTemplate build() {
            return new RetryTemplate(maxAttempts, waitDuration, retryableException, retryListener);
        }
    }
}
