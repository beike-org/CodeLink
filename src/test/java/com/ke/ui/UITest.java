package com.ke.ui;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import javax.swing.*;


public class UITest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }


    public void testPopup() {
        SwingUtilities.invokeLater(()->{
            JFrame frame = new JFrame("TextArea with Chat Popup");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);

            frame.getContentPane().add(new JLabel("test"));
            frame.setVisible(true);
        });

    }

    public void testIdeVersion() {
        System.out.println("MajorVersion:" + ApplicationInfo.getInstance().getMajorVersion());
        System.out.println("ShortVersion:" + ApplicationInfo.getInstance().getShortVersion());
        System.out.println("MinorVersion:" + ApplicationInfo.getInstance().getMinorVersionMainPart());
        System.out.println("FullVersion:" + ApplicationInfo.getInstance().getFullVersion());
        System.out.println("Build:" + ApplicationInfo.getInstance().getBuild());

    }

}
