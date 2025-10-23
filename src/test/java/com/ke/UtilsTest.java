package com.ke;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.ke.utils.RuntimeEnvUtil;


public class UtilsTest extends BasePlatformTestCase {

    private Project project;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String path = "/path/to/project";
        project = ProjectManager.getInstance().loadAndOpenProject(path);
    }


    public void testGetIp() {
        System.out.println(RuntimeEnvUtil.getIp());
    }


    @Override
    protected void tearDown() throws Exception {
        ProjectManager.getInstance().closeAndDispose(project);
        super.tearDown();
    }
}
