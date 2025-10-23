package com.ke.diff;

import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.impl.DiffRequestProcessor;
import com.intellij.diff.impl.DiffWindow;
import com.intellij.diff.util.DiffUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/4/3 14:16
 * @Description
 */
public class UnifiedDiffWindow extends DiffWindow {

    private final DiffRequestChain myRequestChain;

    public UnifiedDiffWindow(@Nullable Project project, @NotNull DiffRequestChain requestChain, @NotNull DiffDialogHints hints) {
        super(project, requestChain, hints);
        this.myRequestChain = requestChain;
    }

    @Override
    protected @NotNull DiffRequestProcessor createProcessor() {
        return new UnifiedDiffRequestChainProcessor(myProject, myRequestChain);
    }

    protected void onAfterNavigate() {
        DiffUtil.closeWindow(getWrapper().getWindow(), true, true);
    }
}
