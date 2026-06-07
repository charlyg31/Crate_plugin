package com.hazebyte.crate.cratereloaded.crate.animationV2.prebuilt;

import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.crate.animationV2.Animation;
import com.hazebyte.crate.cratereloaded.crate.animationV2.AnimationFrame;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import com.hazebyte.crate.cratereloaded.model.RewardV2;
import com.hazebyte.crate.cratereloaded.util.RandomGlassPaneGenerator;
import com.hazebyte.crate.cratereloaded.util.format.ItemFormatter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RouletteAnimationGenerator implements AnimationGenerator {

    public static final int MIDDLE_SLOT = 13;

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
        double base = Math.pow(2, (double) i / 5);
        return (long) Math.min(20, Math.max(1, base));
    }

    private List<AnimationFrame> createAnimationFrames(Player player, CrateV2 crateV2) {
        var frames = new ArrayList<AnimationFrame>();
        for (int i = 0; i <= 20; i++) {
            var rewardV2 = CorePlugin.getJavaPluginComponent()
                    .getGenerateCratePrizeComponent()
                    .generateRewardForAnimation(player, crateV2);
            if (rewardV2 == null) continue;
            long length = durationFormula(i);

            if (i == 20) {
                winningReward = rewardV2;
                frames.add(createWinningFrame(rewardV2, 40L, crateV2));
            } else {
                frames.add(createAnimationFrame(rewardV2, length, crateV2));
            }
        }
        return frames;
    }

    private AnimationFrame createAnimationFrame(RewardV2 rewardV2, long frameLength, CrateV2 crateV2) {
        var animationFrameBuilder = AnimationFrame.builder().frameLength(frameLength);
        for (int i = 0; i < 27; i++) {
            if (i == MIDDLE_SLOT) {
                ItemStack displayItem = rewardV2.getDisplayItem().orElse(new ItemStack(Material.STONE)).clone();
                replaceChancePlaceholder(displayItem, rewardV2, crateV2);
                animationFrameBuilder.itemMapping(i, displayItem);
            } else {
                animationFrameBuilder.itemMapping(i, RandomGlassPaneGenerator.getRandomPane());
            }
        }
        return animationFrameBuilder.build();
    }

    private AnimationFrame createWinningFrame(RewardV2 rewardV2, long frameLength, CrateV2 crateV2) {
        var animationFrameBuilder = AnimationFrame.builder().frameLength(frameLength);
        var staticPane = RandomGlassPaneGenerator.getRandomPane();
        for (int i = 0; i < 27; i++) {
            if (i == MIDDLE_SLOT) {
                ItemStack displayItem = rewardV2.getDisplayItem().orElse(new ItemStack(Material.STONE)).clone();
                replaceChancePlaceholder(displayItem, rewardV2, crateV2);
                animationFrameBuilder.itemMapping(i, displayItem);
            } else {
                animationFrameBuilder.itemMapping(i, staticPane);
            }
        }
        return animationFrameBuilder.build();
    }
    private void replaceChancePlaceholder(ItemStack item, RewardV2 rewardV2, CrateV2 crateV2) {
        double total = crateV2.getRewards().stream().mapToDouble(RewardV2::getChance).sum();
        if (total == 0) return;
        DecimalFormat df = new DecimalFormat(CorePlugin.getPlugin().getSettings().getDecimalFormat());
        double pct = (rewardV2.getChance() / total) * 100;
        String chanceStr = String.format("%s%%", df.format(pct));
        var meta = item.getItemMeta();
        if (meta == null) return;
        if (meta.hasLore()) {
            var lore = meta.getLore();
            lore.replaceAll(s -> s.replace("{chance}", chanceStr));
            meta.setLore(lore);
        }
        if (meta.hasDisplayName()) {
            meta.setDisplayName(meta.getDisplayName().replace("{chance}", chanceStr));
        }
        item.setItemMeta(meta);
    }
}
