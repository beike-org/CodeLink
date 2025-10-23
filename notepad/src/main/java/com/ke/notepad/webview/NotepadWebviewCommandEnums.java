package com.ke.notepad.webview;

import com.ke.webview.WebviewCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/26 17:02
 * @Description
 */
@Getter
@AllArgsConstructor
public enum NotepadWebviewCommandEnums implements WebviewCommand {

    //----------------NotePad-------------------//
    NOTEPAD_FILE_LIST("notepadFileList", "wtp"),
    ADD_NOTEPAD("addNotepad", "wtp");

    private final String command;

    private final String type;
}
