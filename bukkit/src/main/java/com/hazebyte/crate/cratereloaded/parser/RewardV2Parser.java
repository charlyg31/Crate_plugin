package com.hazebyte.crate.cratereloaded.parser;

import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.error.ValidationException;
import com.hazebyte.crate.cratereloaded.model.RewardImpl;
import com.hazebyte.crate.cratereloaded.model.RewardV2;
import com.hazebyte.crate.cratereloaded.util.format.CustomFormat;
import com.hazebyte.crate.cratereloaded.util.item.ItemParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Parses reward configurations into RewardV2 data models.
 * Handles both modern V2 format and legacy reward string format.
 */
@Singleton
public class RewardV2Parser {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("RewardV2Parser");


    @Inject
    public RewardV2Parser() {}

    /**
     * Parses a list of reward strings from YAML configuration.
     *
     * @param section Configuration section containing "reward.rewards" list
     * @param crateName Crate name for error logging
     * @return List of parsed RewardV2 objects
     */
    public List<RewardV2> parseRewardsFromStringList(
            ConfigurationSection section, String crateName) {
        List<String> rewardList = section.getStringList("reward.rewards");
        List<RewardV2> rewards = new ArrayList<>();

        for (String rewardString : rewardList) {
            try {
                rewardString = CustomFormat.format(rewardString);
                RewardV2 reward = parseRewardFromString(rewardString);
                rewards.add(reward);
            } catch (ValidationException e) {
                log.severe(String.format(
                        "Failed to parse reward in crate [%s]: %s\nError: %s", crateName, rewardString, e));
            }
        }

        return rewards;
    }

    /**
     * Parses a single reward from a legacy reward string format.
     * Format example: "item:(DIAMOND 1 name:&aDiamond) chance:(100) commands:(...)"
     *
     * @param rewardString Legacy reward string
     * @return Parsed RewardV2 object
     * @throws ValidationException if parsing fails
     */
    private RewardV2 parseRewardFromString(String rewardString) throws ValidationException {
        // Legacy string format - use existing RewardImpl parser and convert to V2
        // This maintains backwards compatibility with legacy crate configs
        try {
            RewardImpl rewardImpl = new RewardImpl(rewardString);
            return CorePlugin.REWARD_MAPPER.fromImplementation(rewardImpl);
        } catch (Exception e) {
            throw new ValidationException("Failed to parse legacy reward string: " + e.getMessage());
        }
    }

    /**
     * Parses rewards from modern V2 YAML format.
     *
     * @param rewardsSection Configuration section containing reward definitions
     * @return List of parsed RewardV2 objects
     */
    public List<RewardV2> parseRewardsFromSection(ConfigurationSection rewardsSection) {
        List<RewardV2> rewards = new ArrayList<>();

        for (String key : rewardsSection.getKeys(false)) {
            ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(key);
            if (rewardSection != null) {
                try {
                    RewardV2 reward = parseRewardFromSection(rewardSection);
                    rewards.add(reward);
                } catch (Exception e) {
                    log.severe(String.format("Failed to parse reward [%s]: %s", key, e.getMessage()));
                }
            }
        }

        return rewards;
    }

    /**
     * Parses a single reward from a configuration section.
     *
     * @param section Configuration section for the reward
     * @return Parsed RewardV2 object
     */
    private RewardV2 parseRewardFromSection(ConfigurationSection section) {
        return RewardV2.builder()
                .displayItem(parseOptionalDisplayItem(section))
                .chance(section.getDouble("chance", 0.0))
                .items(parseItems(section))
                .commands(parseCommands(section))
                .exclusivePermissions(section.getStringList("exclusive-permissions"))
                .inclusivePermissions(section.getStringList("inclusive-permissions"))
                .broadcastMessage(parseMessages(section, "broadcast-message"))
                .openMessage(parseMessages(section, "open-message"))
                .constant(section.getBoolean("constant", false))
                .unique(section.getBoolean("unique", false))
                .build();
    }

    /**
     * Parses optional display item for the reward.
     */
    private Optional<ItemStack> parseOptionalDisplayItem(ConfigurationSection section) {
        if (!section.isSet("display-item")) {
            return Optional.empty();
        }

        try {
            String itemString = CustomFormat.format(section.getString("display-item"));
            ItemStack item = ItemParser.parse(itemString);
            return Optional.ofNullable(item);
        } catch (ValidationException e) {
            log.warning(String.format("Failed to parse display item: %s", e.getMessage()));
            return Optional.empty();
        }
    }

    /**
     * Parses list of items from configuration section.
     */
    private List<ItemStack> parseItems(ConfigurationSection section) {
        if (!section.isSet("items")) {
            return Collections.emptyList();
        }

        List<ItemStack> items = new ArrayList<>();
        ConfigurationSection itemsSection = section.getConfigurationSection("items");

        if (itemsSection != null) {
            // Items defined as subsections
            for (String key : itemsSection.getKeys(false)) {
                try {
                    String itemString = CustomFormat.format(itemsSection.getString(key));
                    ItemStack item = ItemParser.parse(itemString);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (ValidationException e) {
                    log.warning(String.format("Failed to parse item [%s]: %s", key, e.getMessage()));
                }
            }
        } else if (section.isList("items")) {
            // Items defined as list
            List<String> itemStrings = section.getStringList("items");
            for (String itemString : itemStrings) {
                try {
                    itemString = CustomFormat.format(itemString);
                    ItemStack item = ItemParser.parse(itemString);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (ValidationException e) {
                    log.warning(String.format("Failed to parse item: %s", e.getMessage()));
                }
            }
        }

        return items;
    }

    /**
     * Parses command list from configuration section.
     */
    private List<String> parseCommands(ConfigurationSection section) {
        if (!section.isSet("commands")) {
            return Collections.emptyList();
        }

        if (section.isList("commands")) {
            return section.getStringList("commands");
        } else {
            String command = section.getString("commands", "");
            return Collections.singletonList(command);
        }
    }

    /**
     * Parses message list from configuration section.
     */
    private List<String> parseMessages(ConfigurationSection section, String key) {
        if (!section.isSet(key)) {
            return Collections.emptyList();
        }

        if (section.isList(key)) {
            return section.getStringList(key);
        } else {
            String message = section.getString(key, "");
            return Collections.singletonList(message);
        }
    }
}
