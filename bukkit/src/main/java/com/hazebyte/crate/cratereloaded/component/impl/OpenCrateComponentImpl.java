package com.hazebyte.crate.cratereloaded.component.impl;

import com.google.common.base.Strings;
import com.hazebyte.crate.api.crate.AnimationType;
import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.api.effect.Category;
import com.hazebyte.crate.api.result.RewardExecutorResult;
import com.hazebyte.crate.api.util.Messenger;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.component.GivePlayerItemsComponent;
import com.hazebyte.crate.cratereloaded.component.OpenCrateComponent;
import com.hazebyte.crate.cratereloaded.component.model.CrateOpenRequest;
import com.hazebyte.crate.cratereloaded.component.model.CrateOpenResponse;
import com.hazebyte.crate.cratereloaded.crate.animationV2.Animation;
import com.hazebyte.crate.cratereloaded.crate.animationV2.prebuilt.CsgoAnimationGenerator;
import com.hazebyte.crate.cratereloaded.crate.animationV2.prebuilt.RouletteAnimationGenerator;
import com.hazebyte.crate.cratereloaded.model.CrateImpl;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import com.hazebyte.crate.cratereloaded.model.GiveItemExecutorResult;
import com.hazebyte.crate.cratereloaded.model.RewardImpl;
import com.hazebyte.crate.cratereloaded.model.RewardV2;
import com.hazebyte.crate.cratereloaded.util.CommandUtil;
import com.hazebyte.crate.cratereloaded.util.StringUtils;
import com.hazebyte.crate.cratereloaded.util.format.CustomFormat;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class OpenCrateComponentImpl implements OpenCrateComponent {

    private final CorePlugin plugin;
    private final GivePlayerItemsComponent givePlayerItemsComponent;

    @Inject
    public OpenCrateComponentImpl(CorePlugin plugin, GivePlayerItemsComponent givePlayerItemsComponent) {
        this.plugin = plugin;
        this.givePlayerItemsComponent = givePlayerItemsComponent;
    }
    @Override
    public CrateOpenResponse openCrate(CrateOpenRequest request) {
        // Get CrateV2 (primary) - no conversion needed!
        CrateV2 crateV2 = request.getCrateV2OrConvert();

        // Animation V2 (native CrateV2 usage!)
        if (crateV2.getAnimationType() != null &&
            (crateV2.getAnimationType() == AnimationType.ROULETTE_V2 || crateV2.getAnimationType() == AnimationType.ROULETTE)) {
            Animation animation = new RouletteAnimationGenerator().createAnimation(request.getPlayer(), crateV2);
            CorePlugin.getJavaPluginComponent().getAnimationManager().startAnimation(animation);
            return CrateOpenResponse.builder().build();
        } else if (crateV2.getAnimationType() != null &&
            (crateV2.getAnimationType() == AnimationType.CSGO_V2 || crateV2.getAnimationType() == AnimationType.CSGO
            || crateV2.getAnimationType() == AnimationType.CSGO_REVERSE)) {
            Animation animation = new CsgoAnimationGenerator().createAnimation(request.getPlayer(), crateV2);
            CorePlugin.getJavaPluginComponent().getAnimationManager().startAnimation(animation);
            return CrateOpenResponse.builder().build();
        } else if (crateV2.getAnimation() != null) {
            // Legacy animation - need CrateImpl
            CrateImpl crate = CorePlugin.CRATE_MAPPER.toImplementation(crateV2);
            Inventory inventory = crate.getAnimation().open(request.getPlayer(), request.getLocation());
            if (inventory == null) {
                Messenger.warning(String.format(
                        "Animation has not ended for player: [%s]",
                        request.getPlayer().getName()));
            }
            return CrateOpenResponse.builder().build();
        }

        // Direct rewards (no animation)
        // Convert to CrateImpl for legacy reward system (temporary)
        CrateImpl crate = CorePlugin.CRATE_MAPPER.toImplementation(crateV2);
        List<Reward> rewards =
                plugin.getCrateRegistrar().generateCrateRewards(crate, request.getPlayer());
        Runnable runnable = () -> {
            crate.onRewards(
                    request.getPlayer(),
                    rewards,
                    request.getLocation(),
                    (e) -> crate.runEffect(request.getLocation(), Category.OPEN, request.getPlayer()));
        };
        Bukkit.getScheduler().runTaskLater(plugin, runnable, 3L);

        return CrateOpenResponse.builder().build();
    }

    @Override
    public Set<RewardExecutorResult> executeReward(Player player, Reward reward) {
        Set<RewardExecutorResult> rewardResults = new HashSet<>();

        /* Give items */
        Set<GiveItemExecutorResult> itemResults =
                givePlayerItemsComponent.giveItems(reward.getItems(), player);
        transferItemResults(itemResults, rewardResults);

        /* Run commands */
        CommandUtil.run(reward.getCommands(player));
        if (reward.getCommands(player).size() > 0) {
            rewardResults.add(RewardExecutorResult.COMMANDS_EXECUTED);
        }

        /* Run messages */
        ((RewardImpl) reward).runMessage(player);
        if (reward.getBroadcastMessage().size() > 0 || reward.getOpenMessage().size() > 0) {
            rewardResults.add(RewardExecutorResult.MESSAGES_EXECUTED);
        }
        return rewardResults.isEmpty() ? EnumSet.noneOf(RewardExecutorResult.class) : EnumSet.copyOf(rewardResults);
    }

    @Override
    public void executeRewardV2(Player player, RewardV2 rewardV2) {
        /* Give items */
        givePlayerItemsComponent.giveItems(rewardV2.getItems(), player);

        /* Run commands */
        CommandUtil.run(rewardV2.getCommands().stream()
                .map(cmd -> CustomFormat.format(cmd, player))
                .collect(Collectors.toList()));

        /* Run messages */
        rewardV2.getBroadcastMessage().stream()
                .filter(s -> !Strings.isNullOrEmpty(s))
                .map(s -> CustomFormat.format(s, player, this))
                .forEach(Messenger::broadcast);

        rewardV2.getOpenMessage().stream()
                .filter(s -> !Strings.isNullOrEmpty(s))
                .map(s -> CustomFormat.format(s, player, this))
                .forEach(s -> Messenger.tell(player, s));
    }

    @Override
    public void executeCrateV2Message(Player player, CrateV2 crateV2, List<Reward> rewards) {
        crateV2.getBroadcastMessage().stream()
                .filter(s -> !Strings.isNullOrEmpty(s))
                .map(s -> CustomFormat.format(s, player))
                .map(s -> CustomFormat.format(s, crateV2))
                .map(s -> CustomFormat.format(s, rewards))
                .map(s -> StringUtils.formatString(player, s))
                .forEach(Messenger::broadcast);

        crateV2.getOpenMessage().stream()
                .filter(s -> !Strings.isNullOrEmpty(s))
                .map(s -> CustomFormat.format(s, player))
                .map(s -> CustomFormat.format(s, crateV2))
                .map(s -> CustomFormat.format(s, rewards))
                .map(s -> StringUtils.formatString(player, s))
                .forEach(s -> Messenger.tell((CommandSender) player, s));
    }

    private void transferItemResults(
            Set<GiveItemExecutorResult> itemResults, Set<RewardExecutorResult> rewardExecutorResults) {
        for (GiveItemExecutorResult result : itemResults) {
            switch (result) {
                case PUT_INTO_PLAYER_INVENTORY:
                    rewardExecutorResults.add(RewardExecutorResult.ITEMS_GIVEN_TO_PLAYER);
                    break;
                case PUT_INTO_PLAYER_CLAIM:
                    rewardExecutorResults.add(RewardExecutorResult.ITEMS_SENT_TO_CLAIM);
                    break;
                case DROPPED_TO_WORLD:
                    rewardExecutorResults.add(RewardExecutorResult.ITEMS_DROPPED_TO_WORLD);
                    break;
            }
        }
    }
}
