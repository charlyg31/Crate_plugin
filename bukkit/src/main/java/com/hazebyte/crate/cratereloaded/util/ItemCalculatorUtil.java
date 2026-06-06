package com.hazebyte.crate.cratereloaded.util;

import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemCalculatorUtil {

    private static final int STACK_SIZE = 64;

    /**
     * Return the number of slots required for an itemstack to be put into an inventory. Example: For
     * a size of <64, one slot is needed. For a size of <128, two slots is needed.
     *
     * @param item
     * @return the number of slots needed for an itemstack.
     */
    public static int getNumberOfSlotsRequired(ItemStack item) {
        return getNumberOfSlotsRequired(item, STACK_SIZE);
    }

    private static int getNumberOfSlotsRequired(ItemStack item, int stackSize) {
        int maxStackSize = stackSize;
        int amount = item.getAmount();
        return (int) Math.ceil((double) amount / maxStackSize);
    }

    /**
     * This attempts to put the maximum number of items into the player's inventory.
     *
     * <p>- If the number of slots required is less than the number of slots empty, add all the items
     * in. - Otherwise, attempt to give the maximum number of items the inventory can hold. - If the
     * inventory does not have space, then this function is an no-op.
     *
     * <p>This does not account for stack size.
     *
     * @param player
     * @param item
     * @return the amount of items that have not been given to the player.
     */
    public static Optional<ItemStack> putItemsIntoInventory(
            final Player player, final ItemStack item) {
        final ItemStack cloned = item.clone();
        final int numberOfSlotsRequired = getNumberOfSlotsRequired(cloned, STACK_SIZE);
        final int numberOfSlotsEmpty = PlayerUtil.getSlotsLeft(player);
        if (numberOfSlotsEmpty >= numberOfSlotsRequired) {
            int leftOver = cloned.getAmount();
            while (leftOver > 0) {
                final int currentAmountToGive = leftOver >= STACK_SIZE ? STACK_SIZE : leftOver;
                leftOver = leftOver - currentAmountToGive;
                giveItemToPlayer(cloned, currentAmountToGive, player);
            }
            return Optional.empty();
        }

        if (numberOfSlotsEmpty > 0) {
            int leftOver = cloned.getAmount();
            for (int i = 0; i < numberOfSlotsEmpty; i++) {
                final int currentAmountToGive = leftOver >= STACK_SIZE ? STACK_SIZE : leftOver;
                leftOver = leftOver - currentAmountToGive;
                giveItemToPlayer(cloned, currentAmountToGive, player);
            }

            cloned.setAmount(leftOver);
        }

        return Optional.of(cloned);
    }

    private static void giveItemToPlayer(
            final ItemStack itemStack, final int currentAmountToGive, final Player player) {
        ItemStack currentItem = itemStack.clone();
        currentItem.setAmount(currentAmountToGive);
        player.getInventory().addItem(currentItem);
    }
}
