package com.hazebyte.crate.cratereloaded.claim.storage.yaml;

import com.hazebyte.crate.api.claim.Claim;
import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.claim.ClaimExecutor;
import com.hazebyte.crate.cratereloaded.claim.CrateClaim;
import com.hazebyte.crate.cratereloaded.model.Config;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.bukkit.OfflinePlayer;

/**
 * This reads a claim from a Config using the V1 format.
 *
 * <p>An example of this format is the following: ``` 123456: - 'chance:(0), item:(diamond 1)' ```
 *
 * @return the Claim object from the old config format.
 */
public class V1YamlClaimLineParser implements YamlClaimLineParser {

    private final ClaimExecutor claimExecutor;

    public V1YamlClaimLineParser(ClaimExecutor claimExecutor) {
        this.claimExecutor = claimExecutor;
    }

    @Override
    public Claim apply(@Nonnull OfflinePlayer player, Config config, String key) {
        validateInput(player, config, key);

        String timestampString = key;
        Long timestamp;
        try {
            timestamp = Long.parseLong(timestampString);
        } catch (NumberFormatException ex) {
            timestamp = System.currentTimeMillis();
        }

        List<String> rewardList = config.getConfig().getStringList(key);

        Function<String, Reward> stringToRewardFn =
                s -> CorePlugin.getPlugin().getCrateRegistrar().createReward(s);
        List<Reward> rewards = rewardList.stream().map(stringToRewardFn).collect(Collectors.toList());
        CrateClaim claim = CrateClaim.builder()
                .owner(player)
                .rewards(rewards)
                .executor(claimExecutor)
                .build();
        claim.setTimestamp(timestamp);
        return claim;
    }
}
