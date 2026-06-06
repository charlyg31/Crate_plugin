package com.hazebyte.crate.cratereloaded.crate.animationV2;

import static com.hazebyte.crate.cratereloaded.util.GeneralConstants.FIVE_MINUTES_TICKS;

import com.hazebyte.crate.api.CrateAPI;
import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.api.effect.Category;
import com.hazebyte.crate.api.event.ClaimGiveEvent;
import com.hazebyte.crate.api.util.Messenger;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.claim.ClaimExecutor;
import com.hazebyte.crate.cratereloaded.claim.CrateClaim;
import com.hazebyte.crate.cratereloaded.crate.animationV2.events.AnimationCloseEvent;
import com.hazebyte.crate.cratereloaded.crate.animationV2.events.AnimationFrameChangeEvent;
import com.hazebyte.crate.cratereloaded.util.format.CustomFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AnimationManager {

    private final JavaPlugin plugin;
    private final CorePlugin corePlugin;
    private final ClaimExecutor claimExecutor;
    private final Map<UUID, AnimationExecution> ongoingAnimations;

    public AnimationManager(JavaPlugin plugin, CorePlugin corePlugin, ClaimExecutor claimExecutor) {
        this.plugin = plugin;
        this.corePlugin = corePlugin;
        this.claimExecutor = claimExecutor;
        this.ongoingAnimations = new ConcurrentHashMap<>();

        plugin.getServer().getPluginManager().registerEvents(new AnimationListener(this), plugin);
        plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(
                        plugin,
                        () -> {
                            new ArrayList<>(ongoingAnimations.keySet())
                                    .stream()
                                            .filter(uuid -> {
                                                Player player = Bukkit.getPlayer(uuid);
                                                return player == null
                                                        || !player.isOnline()
                                                        || ongoingAnimations.get(uuid).isCancelled();
                                            })
                                            .forEach(ongoingAnimations::remove);
                        },
                        FIVE_MINUTES_TICKS,
                        FIVE_MINUTES_TICKS);
    }

    public void startAnimation(Animation animation) {
        UUID playerId = animation.getPlayer().getUniqueId();
        if (ongoingAnimations.containsKey(playerId)) {
            AnimationExecution animationExecution = ongoingAnimations.get(playerId);
            if (animationExecution.isCancelled()) {
                ongoingAnimations.remove(playerId);
            } else {
                throw new RuntimeException("Player already has an ongoing animation");
            }
        }

        AnimationExecution animationExecution = new AnimationExecution(animation);
        animationExecution.runTaskTimer(plugin, 0L, 1L);
        ongoingAnimations.put(playerId, animationExecution);
    }

    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (!ongoingAnimations.containsKey(playerId)) {
            return;
        }

        AnimationExecution animationExecution = ongoingAnimations.get(playerId);
        if (animationExecution.isCancelled()) {
            ongoingAnimations.remove(playerId);
            return;
        }

        if (corePlugin.getSettings().allowsInventoryExiting()) {
            animationExecution.cancelAnimation(false);
        } else {
            // this needs to be a scheduler or else an infinite loop will happen.
            // this will close the existing inventory, reopen, close existing...
            Bukkit.getScheduler()
                    .runTaskLater(
                            corePlugin,
                            () -> corePlugin.getJavaPluginComponent()
                                    .getInventoryManager()
                                    .openInventory(animationExecution.getInventoryV2(), player),
                            1L);
        }
    }

    public void onAnimationClose(AnimationCloseEvent event) {
        List<Reward> rewards =
                CorePlugin.REWARD_MAPPER.toImplementation(event.getAnimation().getWinningRewards()).stream()
                        .map(reward -> (Reward) reward)
                        .collect(Collectors.toList());

        Player player = event.getAnimation().getPlayer();
        if (corePlugin.getSettings().isHandlingClaims() && !event.getIsCompleted()) {
            corePlugin.getClaimRegistrar().addClaim(player, rewards);

            if (player.isOnline()) {
                CrateClaim claim = CrateClaim.builder()
                        .owner(player)
                        .rewards(rewards)
                        .executor(claimExecutor)
                        .build();
                ClaimGiveEvent claimGiveEvent = new ClaimGiveEvent(claim);
                Bukkit.getPluginManager().callEvent(claimGiveEvent);

                if (claimGiveEvent.isCancelled()) {
                    return;
                }

                String format = CrateAPI.getMessage("core.claim_animation");
                String message = CustomFormat.format(format, player);

                Messenger.tell(event.getAnimation().getPlayer(), message);
            }
        } else {
            Reward reward = CorePlugin.REWARD_MAPPER.toImplementation(
                    event.getAnimation().getWinningRewards().get(0));
            CorePlugin.getJavaPluginComponent()
                    .getOpenCrateComponent()
                    .executeCrateV2Message(
                            event.getAnimation().getPlayer(),
                            event.getAnimation().getCrateV2(),
                            Collections.singletonList(reward));
            CorePlugin.getJavaPluginComponent()
                    .getOpenCrateComponent()
                    .executeReward(event.getAnimation().getPlayer(), reward);
        }
    }

    public void onFrameChange(AnimationFrameChangeEvent event) {
        var crate = corePlugin.getCrateRegistrar()
                .getCrate(event.getAnimation().getCrateV2().getCrateName());
        var player = event.getAnimation().getPlayer();
        if (event.getCurrentFrameIndex() == event.getAnimation().getFrames().size()) {
            crate.runEffect(player.getLocation(), Category.END, player);
        } else {
            crate.runEffect(player.getLocation(), Category.ANIMATION, player);
        }
    }
}
