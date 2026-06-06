package com.hazebyte.crate.cratereloaded.locale;

import com.hazebyte.crate.cratereloaded.CorePlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LanguageTable {

    private final Locale locale;
    private final Map<MessageKey, String> messages = new HashMap<>();

    LanguageTable(Locale locale) {
        this.locale = locale;
    }

    public String addMessage(MessageKey key, String message) {
        return messages.put(key, message);
    }

    public String getMessage(MessageKey key) {
        return messages.get(key);
    }

    public void addMessages(Map<MessageKey, String> messages) {
        this.messages.putAll(messages);
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean addMessageBundle(String bundleName) {
        try {
            boolean found = false;
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
            for (String key : bundle.keySet()) {
                found = true;
                addMessage(MessageKey.of(key), bundle.getString(key));
            }
            return found;
        } catch (MissingResourceException e) {
            return false;
        }
    }

    public boolean addMessageBundle(String bundleName, String dataFolderName) {
        File file = new File(String.format("%s%s%s", dataFolderName, File.separator, bundleName));
        try (InputStream stream = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            boolean found = false;
            ResourceBundle bundle = new PropertyResourceBundle(reader);
            for (String key : bundle.keySet()) {
                found = true;
                //                Messenger.info(String.format("%s: %s", key, bundle.getString(key)));
                addMessage(MessageKey.of(key), bundle.getString(key));
            }
            return found;
        } catch (Exception e) {
            CorePlugin.getPlugin()
                    .getLogger()
                    .log(
                            java.util.logging.Level.WARNING,
                            String.format("Failed to load message bundle: %s", bundleName),
                            e);
            return false;
        }
    }
}
