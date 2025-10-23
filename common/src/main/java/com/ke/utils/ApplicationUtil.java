package com.ke.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.IdeFrame;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/5 16:34
 * @Description
 */
public class ApplicationUtil {

    @Nullable
    public static Project findCurrentProject() {
        IdeFrame frame = IdeFocusManager.getGlobalInstance().getLastFocusedFrame();
        Project project = frame != null ? frame.getProject() : null;
        if (isValidProject(project)) {
            return project;
        } else {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();

            for (Project p : projects) {
                if (isValidProject(p)) {
                    return p;
                }
            }

            return null;
        }
    }

    public static Iterable<Project> findValidProjects() {
        return Arrays.stream(ProjectManager.getInstance().getOpenProjects()).filter(ApplicationUtil::isValidProject).collect(Collectors.toList());
    }

    private static boolean isValidProject(@Nullable Project project) {
        return project != null && !project.isDisposed() && !project.isDefault();
    }
}
