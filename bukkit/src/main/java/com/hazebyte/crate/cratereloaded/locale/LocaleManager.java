package com.hazebyte.crate.cratereloaded.locale;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocaleManager {

    private Locale defaultLocale;
    private final Map<Locale, LanguageTable> table = new HashMap<>();

    public LocaleManager(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void addMessages(Locale locale, Map<MessageKey, String> messages) {
        getTable(locale).addMessages(messages);
    }

    public String addMessage(Locale locale, MessageKey messageKey, String message) {
        return getTable(locale).addMessage(messageKey, message);
    }

    public boolean addMessageBundle(String bundle, Locale... locales) {
        for (Locale locale : locales) {
            return getTable(locale).addMessageBundle(bundle);
        }
        return false;
    }

    public boolean addMessageBundle(String bundle, String dataFolderName, Locale... locales) {
        for (Locale locale : locales) {
            return getTable(locale).addMessageBundle(bundle, dataFolderName);
        }
        return false;
    }

    public String getMessage(MessageKey key) {
        Locale locale = getDefaultLocale();
        return getTable(locale).getMessage(key);
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale locale) {
        this.defaultLocale = locale;
    }

    public LanguageTable getTable(Locale locale) {
        return table.computeIfAbsent(locale, LanguageTable::new);
    }
}
