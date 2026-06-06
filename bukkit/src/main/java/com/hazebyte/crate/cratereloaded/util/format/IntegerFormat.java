package com.hazebyte.crate.cratereloaded.util.format;


public class IntegerFormat extends Format {

    public IntegerFormat(String message) {
        super(message);
    }

    @Override
    public String format(Object object) {
        if (object instanceof Integer) {
            return format((Integer) object);
        }
        return message;
    }

    public String format(Integer number) {
        message = message.replace("{number}", Integer.toString(number));
        return message;
    }
}
