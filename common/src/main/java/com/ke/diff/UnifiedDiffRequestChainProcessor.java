package com.ke.diff;

import com.intellij.diff.DiffTool;
import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.containers.ContainerUtil;
import com.ke.utils.StoreKeys;
import com.ke.utils.StoreUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/4/3 14:06
 * @Description 优先单边展示
 */
public class UnifiedDiffRequestChainProcessor extends CacheDiffRequestChainProcessor {

    public UnifiedDiffRequestChainProcessor(@Nullable Project project, @NotNull DiffRequestChain requestChain) {
        super(project, requestChain);
    }

    @Override
    public FrameDiffTool.DiffViewer getActiveViewer() {
        return super.getActiveViewer();
    }

    @Override
    protected void setWindowTitle(@NotNull @NlsContexts.DialogTitle String title) {
        super.setWindowTitle(title);
    }


    @Override
    protected @NotNull List<DiffTool> getToolOrderFromSettings(@NotNull List<? extends DiffTool> availableTools) {

        List<DiffTool> result = new ArrayList<>();
        List<String> savedOrder = super.getSettings().getDiffToolsOrder();

        for (String clazz : savedOrder) {
            DiffTool tool = ContainerUtil.find(availableTools, (t) -> t.getClass().getCanonicalName().equals(clazz));
            if (tool != null) {
                // 优先单边展示
                if (tool.getName().contains("Side-by-side")) {
                    String storeKey = StoreUtil.getStore(StoreKeys.AGENTIC_DIFF_CHANGED);
                    if (Objects.nonNull(storeKey) && "true".equals(storeKey)) {
                        result.add(tool);
                    }
                }else {
                    result.add(tool);
                }
            }
        }

        for (DiffTool tool : availableTools) {
            if (!result.contains(tool)) {
                result.add(tool);
            }
        }


        return result;
    }

    @Override
    protected void updateToolOrderSettings(@NotNull List<? extends DiffTool> toolOrder) {
        StoreUtil.putStore(StoreKeys.AGENTIC_DIFF_CHANGED, true);
        super.updateToolOrderSettings(toolOrder);
    }
}
