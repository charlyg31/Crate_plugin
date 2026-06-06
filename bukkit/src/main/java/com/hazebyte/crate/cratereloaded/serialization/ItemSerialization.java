package com.hazebyte.crate.cratereloaded.serialization;

import static com.hazebyte.crate.cratereloaded.util.item.ItemParser.isPotion;
import static com.hazebyte.crate.cratereloaded.util.item.ItemParser.isTippedArrow;

import com.hazebyte.crate.api.util.ItemBuilder;
import com.hazebyte.crate.api.util.Messenger;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.util.StringUtils;
import com.hazebyte.crate.cratereloaded.util.item.ItemUtil;
import com.hazebyte.util.Mat;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

public class ItemSerialization {

    public static final String NAME = "name";
    public static final String LORE = "lore";
    public static final String CUSTOMMODELDATA = "custommodeldata";
    public static final String EFFECT = "effect";
    public static final String POWER = "power";
    public static final String DURATION = "duration";
    public static final String SPLASH = "splash";
    public static final String SKULL = "skull";
    public static final String COLOR = "color";
    public static final String TAG = "tag";

    private static StringBuilder appendData(StringBuilder stringBuilder, Object data) {
        return stringBuilder.append(data).append(" ");
    }

    private static StringBuilder appendData(
            StringBuilder stringBuilder, String key, Object data) {
        return stringBuilder.append(String.format("%s:", key)).append(data).append(" ");
    }

    private static void appendItemMeta(StringBuilder stringBuilder, ItemStack item) {
        if (!item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName()) {
            appendData(
                    stringBuilder,
                    NAME,
                    StringUtils.normaliseHex(meta.getDisplayName().replace(" ", "_")));
        }

        if (meta.hasLore()) {
            StringBuilder lore = new StringBuilder();
            for (String loreLine : meta.getLore()) {
                lore.append(loreLine.replaceAll(" ", "_")).append("|");
            }
            lore = new StringBuilder(lore.substring(0, lore.length() - 1));
            appendData(stringBuilder, LORE, lore);
        }

        if (ItemBuilder.of(item).hasCustomModelData()) {
            int customModelData = ItemBuilder.of(item).getCustomModelData();
            appendData(stringBuilder, CUSTOMMODELDATA, customModelData);
        }

        Map<Enchantment, Integer> enchants = item.getType() == Material.ENCHANTED_BOOK
                ? ((EnchantmentStorageMeta) meta).getStoredEnchants()
                : meta.getEnchants();
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            String message = entry.getKey().getName().toLowerCase() + ":" + entry.getValue();
            appendData(stringBuilder, message);
        }

        if (!meta.getItemFlags().isEmpty()) {
            appendData(stringBuilder, "hide:true");
        }

        if (ItemUtil.isUnbreakable(meta)) { // 1.8 Missing: meta.isUnbreakable
            appendData(stringBuilder, "unbreakable:true");
        }
    }

    private static void appendPotionOrTippedArrowMeta(
            StringBuilder stringBuilder, ItemStack itemStack) {
        if (!isPotion(itemStack) && !isTippedArrow(itemStack)) {
            return;
        }

        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        if (potionMeta.hasCustomEffects()) {
            for (PotionEffect effect : potionMeta.getCustomEffects()) {
                appendData(stringBuilder, EFFECT, effect.getType().getName());
                appendData(stringBuilder, POWER, effect.getAmplifier());
                appendData(stringBuilder, DURATION, effect.getDuration() / 20);
                appendData(stringBuilder, SPLASH, itemStack.getType() == Material.SPLASH_POTION);
            }
        } else { // This is a vanilla potion
            // A potion is extended if the time is increased from 45s to 1m 30s
            // A potion is upgraded if it is AMPLIFIER II instead of I
            PotionData potionData = potionMeta.getBasePotionData();
            appendData(
                    stringBuilder, EFFECT, potionData.getType().getEffectType().getName());
            appendData(stringBuilder, POWER, potionData.isUpgraded() ? 2 : 1);
            appendData(stringBuilder, DURATION, potionData.isExtended() ? 90 * 20 : 45 * 20);
            appendData(stringBuilder, SPLASH, itemStack.getType() == Material.SPLASH_POTION);
        }
    }

    private static void appendPlayerHead(StringBuilder stringBuilder, ItemStack itemStack) {
        if (itemStack.getType() != Mat.PLAYER_HEAD.toMaterial()) {
            return;
        }

        //        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        //        String encoding = HeadTexture.fromMeta(skullMeta);
        //        if (!Strings.isNullOrEmpty(encoding)) {
        //            appendData(stringBuilder, SKULL, encoding);
        //        }
    }

    private static void appendColorData(StringBuilder stringBuilder, ItemStack itemStack) {
        Material material = itemStack.getType();
        switch (material) {
            case LEATHER_HELMET:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) itemStack.getItemMeta();
                Color color = leatherArmorMeta.getColor();
                appendData(
                        stringBuilder,
                        COLOR,
                        String.format("%s,%s,%s", color.getRed(), color.getGreen(), color.getBlue()));
                break;
            case WRITTEN_BOOK:
            case SHIELD:
                // TODO: Support these items
                Messenger.warning("Book and shield serialization is not yet implemented.");
                break;
        }
    }

    private static void appendNbtData(StringBuilder stringBuilder, ItemStack itemStack) {
        if (CorePlugin.getPlugin().getServerVersion().isMockServer()) {
            return;
        }
        try {
            ReadWriteNBT nbt = NBT.itemStackToNBT(itemStack);
            ReadWriteNBT nbtTag = nbt.getCompound(TAG);

            appendData(stringBuilder, nbtTag);
        } catch (Exception ignored) {
            Messenger.warning(
                    String.format("There was an error with NBT serialization for [%s]", itemStack.toString()));
        }
    }

    public static String serialize(ItemStack item) {
        StringBuilder stringBuilder = new StringBuilder();
        Material material = item.getType();

        appendData(stringBuilder, material.name());
        appendData(stringBuilder, item.getAmount());
        if (item.getDurability() != 0) {
            appendData(stringBuilder, String.format("durability:%s", item.getDurability()));
        }
        appendItemMeta(stringBuilder, item);
        appendPotionOrTippedArrowMeta(stringBuilder, item);
        appendPlayerHead(stringBuilder, item);
        appendColorData(stringBuilder, item);
        //        appendNbtData(stringBuilder, item);

        return stringBuilder.toString().trim().replace("§", "&");
    }
}
