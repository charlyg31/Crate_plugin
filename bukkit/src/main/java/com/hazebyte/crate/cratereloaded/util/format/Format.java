package com.hazebyte.crate.cratereloaded.util.format;


public abstract class Format {

    protected String message;

    public Format(String message) {
        this.message = message;
    }

    public abstract String format(Object object);
}
