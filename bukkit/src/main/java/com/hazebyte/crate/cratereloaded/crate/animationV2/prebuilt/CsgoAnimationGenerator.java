package com.hazebyte.crate.cratereloaded.crate.animationV2.prebuilt;

import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.crate.animationV2.Animation;
import com.hazebyte.crate.cratereloaded.crate.animationV2.AnimationFrame;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import com.hazebyte.crate.cratereloaded.model.RewardV2;
import com.hazebyte.crate.cratereloaded.util.InventoryConstants;
import com.hazebyte.crate.cratereloaded.util.RandomGlassPaneGenerator;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CsgoAnimationGenerator implements AnimationGenerator {

    private RewardV2 winningReward;

    @Override
    public Animation createAnimation(Player player, CrateV2 crateV2) {
        var frames = createAnimationFrames(player, crateV2);
        return Animation.builder()
                .player(player)
                .crateV2(crateV2)
                .winningReward(winningReward)
                .frames(frames)
                .build();
    }

    private long durationFormula(int i) {
        double base = Math.pow(2, (double) i / 8);
        return (long) Math.min(40, Math.max(1, base));
    }

    private List<AnimationFrame> createAnimationFrames(Player player, CrateV2 crateV2) {
        var frames = new ArrayList<AnimationFrame>();
        int numberOfFrames = 35;
        for (int i = 0; i <= numberOfFrames; i++) {
            var rewardV2 = CorePlugin.getJavaPluginComponent()
                    .getGenerateCratePrizeComponent()
                    .generateRewardForAnimation(player, crateV2);
            long length = durationFormula(i);

            var frame = createAnimationFrame(rewardV2, length, frames);
            frames.add(frame);
            if (i == numberOfFrames - 4) {
                winningReward = rewardV2;
            }
        }

        if (winningReward != null) {
            var endFrame = createEndAnimationFrame(winningReward, 50L);
            frames.add(endFrame);
        }
        return frames;
    }

    private AnimationFrame createAnimationFrame(
            RewardV2 rewardV2, long frameLength, List<AnimationFrame> currentFrames) {
        var animationFrameBuilder = AnimationFrame.builder().frameLength(frameLength);
        for (int i = 0; i < 27; i++) {
            if (i == 9) {
                animationFrameBuilder.itemMapping(i, rewardV2.getDisplayItem().orElse(new ItemStack(Material.STONE)));
            } else if (i > 9 && i <= 17) {
                if (currentFrames.size() > 0) {
                    AnimationFrame previousFrame = currentFrames.get(currentFrames.size() - 1);
                    animationFrameBuilder.itemMapping(
                            i, previousFrame.getItemMappings().get(i - 1));
                }
            } else {
                animationFrameBuilder.itemMapping(i, RandomGlassPaneGenerator.getRandomPane());
            }
        }
        return animationFrameBuilder.build();
    }

    private AnimationFrame createEndAnimationFrame(RewardV2 rewardV2, long frameLength) {
        var animationFrameBuilder = AnimationFrame.builder().frameLength(frameLength);
        for (int i = 0; i < 27; i++) {
            if (i == InventoryConstants.CENTER_SLOT_THREE_ROWS) {
                animationFrameBuilder.itemMapping(i, rewardV2.getDisplayItem().orElse(new ItemStack(Material.STONE)));
            } else {
                animationFrameBuilder.itemMapping(i, RandomGlassPaneGenerator.getRandomPane());
            }
        }
        return animationFrameBuilder.build();
    }
}
