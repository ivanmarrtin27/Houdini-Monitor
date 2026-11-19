package com.houdini.monitor.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {

    private static final String BASE = "messages";
    private ResourceBundle bundle;

    public Messages(Locale locale) {
        setLocale(locale);
    }

    public void setLocale(Locale locale) {
        Locale effective = locale == null ? Locale.forLanguageTag("es") : locale;
        this.bundle = ResourceBundle.getBundle(BASE, effective);
    }

    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            return key;
        }
    }

    public String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }

    public Locale locale() {
        return bundle.getLocale();
    }
}
