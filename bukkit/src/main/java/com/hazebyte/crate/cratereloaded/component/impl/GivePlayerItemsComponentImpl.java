package com.hazebyte.crate.cratereloaded.component.impl;

import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.component.GivePlayerItemsComponent;
import com.hazebyte.crate.cratereloaded.component.PluginSettingComponent;
import com.hazebyte.crate.cratereloaded.model.GiveItemExecutorResult;
import com.hazebyte.crate.cratereloaded.model.RewardImpl;
import com.hazebyte.crate.cratereloaded.util.ItemCalculatorUtil;
import com.hazebyte.crate.cratereloaded.util.RewardDisplayItemFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GivePlayerItemsComponentImpl implements GivePlayerItemsComponent {

    private final CorePlugin plugin;
    private final PluginSettingComponent settings;

    @Inject
    public GivePlayerItemsComponentImpl(CorePlugin plugin, PluginSettingComponent settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public Set<GiveItemExecutorResult> giveItems(List<ItemStack> items, Player player) {
        Set<GiveItemExecutorResult> result = new HashSet<>();
        for (ItemStack item : items) {
            Optional<ItemStack> leftOverOptional = ItemCalculatorUtil.putItemsIntoInventory(player, item);
            if (!leftOverOptional.isPresent()) {
                result.add(GiveItemExecutorResult.PUT_INTO_PLAYER_INVENTORY);
                continue;
            }

            ItemStack leftOverItem = leftOverOptional.get();
            if (!leftOverItem.equals(item)) {
                result.add(GiveItemExecutorResult.PUT_INTO_PLAYER_INVENTORY);
            }

            if (settings.isHandlingClaims()) {
                Reward reward = new RewardImpl();
                reward.setItems(Arrays.asList(leftOverItem));
                ItemStack displayItem =
                        RewardDisplayItemFactory.createDisplayItem(leftOverItem.clone(), leftOverItem.getAmount());
                reward.setDisplayItem(displayItem);
                plugin.getClaimRegistrar().addClaim(player, Collections.singletonList(reward));
                result.add(GiveItemExecutorResult.PUT_INTO_PLAYER_CLAIM);
                continue;
            }

            player.getWorld().dropItemNaturally(player.getLocation(), leftOverItem);
            result.add(GiveItemExecutorResult.DROPPED_TO_WORLD);
        }
        return result.isEmpty() ? EnumSet.noneOf(GiveItemExecutorResult.class) : EnumSet.copyOf(result);
    }
}
