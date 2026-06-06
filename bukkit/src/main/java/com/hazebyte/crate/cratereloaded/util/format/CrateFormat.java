package com.hazebyte.crate.cratereloaded.util.format;

import com.hazebyte.crate.api.crate.Crate;

public class CrateFormat extends Format {

    public CrateFormat(String message) {
        super(message);
    }

    @Override
    public String format(Object object) {
        if (object instanceof Crate) {
            return format((Crate) object);
        }
        return message;
    }

    public String format(Crate crate) {
        message = message.replace("{crate-name}", crate.getDisplayName())
                .replace("{crate}", crate.getCrateName())
                .replace("{type}", crate.getType().name())
                .replace("{cost}", Double.toString(crate.getCost()));
        ItemFormat format = new ItemFormat(message);
        message = format.format(crate.getDisplayItem());
        return message;
    }
}
