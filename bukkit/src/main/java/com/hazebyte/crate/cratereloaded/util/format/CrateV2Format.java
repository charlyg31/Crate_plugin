package com.hazebyte.crate.cratereloaded.util.format;

import com.hazebyte.crate.cratereloaded.model.CrateV2;

public class CrateV2Format extends Format {

    public CrateV2Format(String message) {
        super(message);
    }

    @Override
    public String format(Object object) {
        if (object instanceof CrateV2) {
            return format((CrateV2) object);
        }
        return message;
    }

    public String format(CrateV2 crate) {
        message = message.replace("{crate-name}", crate.getDisplayName().orElse(""))
                .replace("{crate}", crate.getCrateName())
                .replace("{type}", crate.getType().name())
                .replace("{cost}", Double.toString(crate.getSalePrice()));
        //        ItemFormat format = new ItemFormat(message);
        //        message = format.format(crate.getDisplayItem());
        return message;
    }
}
