package com.hazebyte.crate.cratereloaded.model.menuV2;

import static com.hazebyte.crate.cratereloaded.model.menuV2.InventoryMenusV2.getCrateAdminMenu;
import static com.hazebyte.crate.cratereloaded.model.menuV2.InventoryMenusV2.getListCrateRewardsMenu;

import com.hazebyte.crate.api.util.ItemBuilder;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.menuV2.InventoryButtonV2;
import com.hazebyte.crate.cratereloaded.menuV2.InventoryV2;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import com.hazebyte.crate.cratereloaded.model.RewardV2;
import com.hazebyte.util.Mat;
import java.util.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryButtonsV2 {

    private static String CLOSE_BUTTON_NAME_FORMAT = "%sClose Inventory.";
    private static String PREVIOUS_PAGE_BUTTON_NAME_FORMAT = "%sPrevious Page.";
    private static String NEXT_PAGE_BUTTON_NAME_FORMAT = "%sNext Page.";

    public static InventoryButtonV2 createCloseButton() {
        return InventoryButtonV2.builder()
                .itemCreator(player -> new ItemBuilder(Mat.MUSIC_DISC_11.toMaterial())
                        .displayName(String.format(CLOSE_BUTTON_NAME_FORMAT, ChatColor.RED))
                        .asItemStack())
                .clickHandler(event -> event.getWhoClicked().closeInventory())
                .build();
    }

    public static InventoryButtonV2 createPageNavigationButton(
            InventoryV2 inventoryV2, Function<Player, ItemStack> itemStackCreator) {
        return InventoryButtonV2.builder()
                .itemCreator(player -> itemStackCreator.apply(player))
                .clickHandler(event -> CorePlugin.getJavaPluginComponent()
                        .getInventoryManager()
                        .openInventory(inventoryV2, (Player) event.getWhoClicked()))
                .build();
    }

    public static InventoryButtonV2 createPreviousPageButton(InventoryV2 inventoryV2) {
        return createPageNavigationButton(inventoryV2, (player) -> new ItemBuilder(Mat.OAK_FENCE_GATE.toMaterial())
                .displayName(String.format(PREVIOUS_PAGE_BUTTON_NAME_FORMAT, ChatColor.YELLOW))
                .asItemStack());
    }

    public static InventoryButtonV2 createNextPageButton(InventoryV2 inventoryV2) {
        return createPageNavigationButton(inventoryV2, (player) -> new ItemBuilder(Mat.OAK_FENCE_GATE.toMaterial())
                .displayName(String.format(NEXT_PAGE_BUTTON_NAME_FORMAT, ChatColor.GREEN))
                .asItemStack());
    }

    public static InventoryButtonV2 createNotImplementedButton() {
        return InventoryButtonV2.builder()
                .itemCreator(player -> new ItemBuilder(Mat.BARRIER.toMaterial())
                        .displayName(String.format("%sButton is not implemented.", ChatColor.RED))
                        .asItemStack())
                .build();
    }

    public static InventoryButtonV2 createCrateAdminPageButton(CrateV2 crateV2) {
        InventoryV2 inventoryV2 = getCrateAdminMenu(crateV2);
        Function<Player, ItemStack> itemCreator = (player) -> ItemBuilder.of(
                        crateV2.getDisplayItem().orElse(crateV2.getItem()).clone())
                .append("&a")
                .append(String.format("%s[PLUGIN] Click to show settings.", ChatColor.GREEN))
                .asItemStack();
        return createPageNavigationButton(inventoryV2, itemCreator);
    }

    public static InventoryButtonV2 createListCrateRewardsAdminPageButton(CrateV2 crateV2) {
        InventoryV2 inventoryV2 = getListCrateRewardsMenu(crateV2);
        Function<Player, ItemStack> itemCreator = (player) -> ItemBuilder.of(
                        crateV2.getDisplayItem().orElse(crateV2.getItem()).clone())
                .append("&a")
                .append(String.format("%s[PLUGIN] Click to see all rewards.", ChatColor.GREEN))
                .asItemStack();
        return createPageNavigationButton(inventoryV2, itemCreator);
    }

    public static InventoryButtonV2 createExecuteRewardButton(RewardV2 rewardV2) {
        Function<Player, ItemStack> itemCreator =
                player -> ItemBuilder.of(rewardV2.getDisplayItem().orElse(createDefaultRewardItem(rewardV2)))
                        .asItemStack();

        return InventoryButtonV2.builder()
                .itemCreator(itemCreator)
                .clickHandler(event -> CorePlugin.getJavaPluginComponent()
                        .getOpenCrateComponent()
                        .executeRewardV2((Player) event.getWhoClicked(), rewardV2))
                .build();
    }

    private static ItemStack createDefaultRewardItem(RewardV2 rewardV2) {
        return new ItemBuilder(Mat.CHEST.toMaterial())
                .displayName(String.format("%s[PLUGIN] Display item does not exist.", ChatColor.RED))
                .lore("&a", String.format("%s[PLUGIN] Click to execute the reward.", ChatColor.GREEN))
                .asItemStack();
    }
}
