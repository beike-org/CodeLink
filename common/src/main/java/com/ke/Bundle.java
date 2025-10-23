package com.ke;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.Locale;
import java.util.ResourceBundle;

public class Bundle extends DynamicBundle {
    public static final Bundle INSTANCE = new Bundle();
    private static final String BUNDLE = "messages.CodeLink";
    private static final Locale currentLocale = Locale.getDefault().getLanguage().startsWith("zh") ? Locale.CHINESE : Locale.ENGLISH;

    private Bundle() {
        super(BUNDLE);
    }

    public static String get(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key) {
        return INSTANCE.getMessage(key, currentLocale);
    }

    public static String get(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, currentLocale, params);
    }

    @Override
    protected ResourceBundle findBundle(@NotNull String pathToBundle, @NotNull ClassLoader loader, @NotNull ResourceBundle.Control control) {
        return ResourceBundle.getBundle(pathToBundle, currentLocale);
    }
}
