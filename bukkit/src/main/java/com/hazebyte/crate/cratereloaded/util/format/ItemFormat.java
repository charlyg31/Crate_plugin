package com.hazebyte.crate.cratereloaded.util.format;

import com.hazebyte.crate.cratereloaded.util.MoreObjects;
import com.hazebyte.crate.cratereloaded.util.item.ItemUtil;
import com.hazebyte.crate.utils.WordUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class ItemFormat extends Format {

    public ItemFormat(String message) {
        super(message);
    }

    @Override
    public String format(Object object) {
        if (object instanceof ItemStack) {
            return format((ItemStack) object);
        } else if (object instanceof List) {
            return format((List) object);
        }
        return message;
    }

    public String format(ItemStack item) {
        if (!ItemUtil.isNull(item)) {
            message = message.replace(
                    "{item}",
                    ItemUtil.hasName(item)
                            ? ItemUtil.getName(item)
                            : WordUtils.capitalizeFully(item.getType().name()));
            message = message.replace(
                    "{material}", WordUtils.capitalizeFully(item.getType().name()));
            message = message.replace("{amount}", item.getAmount() > 0 ? Integer.toString(item.getAmount()) : "None");
        }

        if (message.contains("{lore:")) {
            int start = message.indexOf("{lore:") + 1;
            int end = message.indexOf("}", start);
            String sub = message.substring(start, end);
            String[] parts = sub.split(":");
            List<String> lore = ItemUtil.getLore(item);
            String loreLine = "";
            try {
                int number = Integer.parseInt(parts[1]);
                loreLine = lore.get(number);
            } catch (Exception exc) {
                /* Silent */
            }
            message = message.substring(0, start - 1) + loreLine + message.substring(end + 1);
        }
        return message;
    }

    public String format(List<ItemStack> items) {
        if (items.size() > 1) {
            List<String> names = new ArrayList<>();
            for (ItemStack item : items) {
                if (ItemUtil.isNull(item)) continue;

                if (ItemUtil.hasName(item)) {
                    names.add(ItemUtil.getName(item));
                } else {
                    String name = item.getType().toString().replace("_", " ");
                    names.add(WordUtils.capitalizeFully(name));
                }
            }
            message = message.replace("{item}", String.join(", ", names));
        } else {
            ItemStack firstItem = MoreObjects.firstNonNull(items);
            message = format(firstItem);
        }
        return message;
    }
}
