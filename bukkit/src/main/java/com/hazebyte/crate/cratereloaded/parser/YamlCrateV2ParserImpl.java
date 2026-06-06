package com.hazebyte.crate.cratereloaded.parser;

import com.hazebyte.crate.Error;
import com.hazebyte.crate.api.crate.AnimationType;
import com.hazebyte.crate.api.crate.CrateType;
import com.hazebyte.crate.api.crate.EndAnimationType;
import com.hazebyte.crate.api.effect.Category;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.component.PluginSettingComponent;
import com.hazebyte.crate.cratereloaded.error.ValidationException;
import com.hazebyte.crate.cratereloaded.model.Config;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import com.hazebyte.crate.cratereloaded.model.RewardV2;
import com.hazebyte.crate.cratereloaded.util.ConfigConstants;
import com.hazebyte.crate.cratereloaded.util.TypeTranslator;
import com.hazebyte.crate.cratereloaded.util.format.CustomFormat;
import com.hazebyte.crate.cratereloaded.util.item.ItemParser;
import com.hazebyte.crate.cratereloaded.validation.CrateValidatorImpl;
import com.hazebyte.crate.validation.ValidationResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Parses YAML crate configurations directly into CrateV2 data models.
 * This parser creates pure data models without service dependencies.
 */
@Singleton
public class YamlCrateV2ParserImpl {
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("YamlCrateV2ParserImpl");


    // Config Keys
    private static final String BUY_COST = "buy.cost";
    private static final String CONFIRMATION_ENABLED = "confirmation.enabled";
    private static final String CONFIRMATION_ACCEPT_BUTTON = "confirmation.accept-button";
    private static final String CONFIRMATION_DECLINE_BUTTON = "confirmation.decline-button";
    private static final String DISPLAY_ITEM = "display-item";
    private static final String DISPLAY_NAME = "display-name";
    private static final String BUY_ENABLED = "buy.enabled";
    private static final String REWARD_MINIMUM_REWARDS = "reward.minimum-rewards";
    private static final String REWARD_MAXIMUM_REWARDS = "reward.maximum-rewards";
    private static final String PREVIEW_ROWS = "preview.rows";
    private static final String MESSAGE_OPEN = "message.open";
    private static final String MESSAGE_BROADCAST = "message.broadcast";
    private static final String HOLOGRAPHIC_TEXT = "holographic-text";

    private final CorePlugin plugin;
    private final PluginSettingComponent settings;
    private final RewardV2Parser rewardParser;
    private final CrateValidatorImpl crateValidator;

    @Inject
    public YamlCrateV2ParserImpl(
            CorePlugin plugin,
            RewardV2Parser rewardParser,
            CrateValidatorImpl crateValidator,
            PluginSettingComponent settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.rewardParser = rewardParser;
        this.crateValidator = crateValidator;
    }

    /**
     * Parses all crates from a configuration file.
     *
     * @param config Configuration storage
     * @return List of parsed CrateV2 objects
     */
    public List<CrateV2> parse(Config config) {
        log.fine(String.format("Reading Crate Config File (V2): %s", config.getFile().getName()));

        if (config.getConfig() == null) {
            throw new RuntimeException("Storage Configuration is not set up correctly");
        }

        List<CrateV2> crates = new ArrayList<>();
        for (String crateName : config.getConfig().getKeys(false)) {
            try {
                CrateV2 crate = parseCrate(crateName, config);
                if (crate != null) {
                    crates.add(crate);
                } else {
                    log.warning(String.format("Failed to parse crate [%s] - null result", crateName));
                }
            } catch (Exception e) {
                log.log(
                        java.util.logging.Level.SEVERE,
                        String.format(
                                "Failed to parse crate [%s] from file [%s]",
                                crateName, config.getFile().getName()),
                        e);
            }
        }

        return crates;
    }

    /**
     * Parses a single crate from configuration.
     *
     * @param crateName Name of the crate section
     * @param config Configuration storage
     * @return Parsed CrateV2 object
     */
    public CrateV2 parseCrate(String crateName, Config config) {
        ConfigurationSection section = config.getConfig().getConfigurationSection(crateName);

        if (section == null) {
            log.severe(String.format(
                    "Crate section [%s] is invalid in file [%s]", crateName, config.getFile().getName()));
            return null;
        }

        try {
            CrateV2 crate = CrateV2.builder()
                    .crateName(crateName)
                    .uuid(parseUUID(section))
                    .displayName(parseOptionalDisplayName(section))
                    .displayItem(parseOptionalDisplayItem(section, crateName))
                    .animationType(parseAnimationType(section))
                    .endAnimationType(parseEndAnimationType(section))
                    .animation(null) // Animation objects created at runtime
                    .type(parseCrateType(section))
                    .item(parseCrateItem(section, crateName))
                    .salePrice(section.getDouble(BUY_COST, 0.0))
                    .forSale(section.getBoolean(BUY_ENABLED, false))
                    .openMessage(parseOpenMessage(section))
                    .broadcastMessage(parseBroadcastMessage(section))
                    .previewRows(section.getInt(PREVIEW_ROWS, 3))
                    .confirmBeforeUse(section.getBoolean(CONFIRMATION_ENABLED, false))
                    .acceptButton(parseAcceptButton(section))
                    .declineButton(parseDeclineButton(section))
                    .minimumRewards(section.getInt(REWARD_MINIMUM_REWARDS, 1))
                    .maximumRewards(section.getInt(REWARD_MAXIMUM_REWARDS, 1))
                    .rewards(parseRewards(section, crateName))
                    .constantRewards(parseConstantRewards(section, crateName))
                    .holographicText(parseHolographicText(section))
                    .effectCategoryToId(parseEffects(section, crateName))
                    .build();

            // Validate crate configuration at parse-time
            ValidationResult validationResult = crateValidator.validate(crate);
            if (!validationResult.isValid()) {
                log.severe(String.format(
                        "Crate [%s] failed validation in file [%s]:", crateName, config.getFile().getName()));
                validationResult.getErrors().forEach(error -> log.severe("  - " + error.getMessage()));
                return null;
            }

            return crate;
        } catch (Exception e) {
            log.log(
                    java.util.logging.Level.SEVERE,
                    String.format("Error building CrateV2 [%s]", crateName),
                    e);
            return null;
        }
    }

    private String parseUUID(ConfigurationSection section) {
        return section.getString("uuid", UUID.randomUUID().toString());
    }

    private Optional<String> parseOptionalDisplayName(ConfigurationSection section) {
        String displayName = section.getString(DISPLAY_NAME);
        if (displayName == null || displayName.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(CustomFormat.format(displayName));
    }

    private Optional<ItemStack> parseOptionalDisplayItem(
            ConfigurationSection section, String crateName) {
        if (!section.isSet(DISPLAY_ITEM)) {
            return Optional.empty();
        }

        try {
            String itemString = CustomFormat.format(section.getString(DISPLAY_ITEM));
            ItemStack item = ItemParser.parse(itemString);
            return Optional.ofNullable(item);
        } catch (ValidationException e) {
            log.severe(String.format("Failed to parse display item for crate [%s]: %s", crateName, e));
            return Optional.empty();
        }
    }

    private CrateType parseCrateType(ConfigurationSection section) {
        String typeString = section.getString("type", "KEY").toUpperCase();

        try {
            return TypeTranslator.translateCrate(typeString);
        } catch (Exception e) {
            log.warning(String.format("Invalid crate type [%s], defaulting to KEY: %s", typeString, e));
            return CrateType.KEY;
        }
    }

    private AnimationType parseAnimationType(ConfigurationSection section) {
        String animationString = section.getString("animation", "none");
        AnimationType animationType = TypeTranslator.translateAnimation(animationString);

        if (animationType == null) {
            log.warning(String.format("Invalid animation type [%s], defaulting to NONE", animationString));
            return AnimationType.NONE;
        }

        return animationType;
    }

    private EndAnimationType parseEndAnimationType(ConfigurationSection section) {
        String endAnimationString = section.getString("end-animation", "BLANK");
        EndAnimationType endAnimationType = TypeTranslator.translateEndAnimation(endAnimationString);

        if (endAnimationType == null) {
            log.warning(String.format("Invalid end animation type [%s], defaulting to BLANK", endAnimationString));
            return EndAnimationType.BLANK;
        }

        return endAnimationType;
    }

    private ItemStack parseCrateItem(ConfigurationSection section, String crateName) {
        String itemString = section.getString("item");
        if (itemString == null || itemString.isEmpty()) {
            log.warning(String.format("Crate [%s] has no item defined", crateName));
            return null;
        }

        try {
            // Note: Cannot use CustomFormat with crate context here since we're building the crate
            // This is intentional - crate item formatting happens after construction
            itemString = CustomFormat.format(itemString);
            return ItemParser.parse(itemString);
        } catch (ValidationException e) {
            log.severe(String.format("Failed to parse crate item for [%s]: %s", crateName, e));
            return null;
        }
    }

    private List<String> parseOpenMessage(ConfigurationSection section) {
        return parseMessageList(section, MESSAGE_OPEN);
    }

    private List<String> parseBroadcastMessage(ConfigurationSection section) {
        return parseMessageList(section, MESSAGE_BROADCAST);
    }

    private List<String> parseMessageList(ConfigurationSection section, String key) {
        if (!section.isSet(key)) {
            return Collections.emptyList();
        }

        if (section.isList(key)) {
            return section.getStringList(key);
        } else {
            String message = section.getString(key, "");
            return Arrays.asList(message.split("\\\\n"));
        }
    }

    private ItemStack parseAcceptButton(ConfigurationSection section) {
        return parseButton(section, CONFIRMATION_ACCEPT_BUTTON, settings.getClaimAcceptButton());
    }

    private ItemStack parseDeclineButton(ConfigurationSection section) {
        return parseButton(section, CONFIRMATION_DECLINE_BUTTON, settings.getClaimDeclineButton());
    }

    /**
     * Parses a button ItemStack with fallback to global settings.
     *
     * @param section Crate configuration section
     * @param key Button key in config
     * @param defaultButton Default button from global settings
     * @return Parsed or default button
     */
    private ItemStack parseButton(
            ConfigurationSection section, String key, ItemStack defaultButton) {
        if (!section.isSet(key)) {
            return defaultButton;
        }

        try {
            String buttonString = section.getString(key);
            return ItemParser.parse(buttonString);
        } catch (ValidationException e) {
            log.warning(String.format("Failed to parse button [%s], using default: %s", key, e));
            return defaultButton;
        }
    }

    private List<RewardV2> parseRewards(ConfigurationSection section, String crateName) {
        // Check for modern V2 format first (rewards as sections)
        if (section.isConfigurationSection("rewards")) {
            ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");
            return rewardParser.parseRewardsFromSection(rewardsSection);
        }

        // Fall back to legacy format (reward.rewards as string list)
        if (section.isList("reward.rewards")) {
            return rewardParser.parseRewardsFromStringList(section, crateName);
        }

        log.warning(String.format("Crate [%s] has no rewards defined", crateName));
        return Collections.emptyList();
    }

    private List<RewardV2> parseConstantRewards(ConfigurationSection section, String crateName) {
        if (!section.isConfigurationSection("constant-rewards")) {
            return Collections.emptyList();
        }

        ConfigurationSection constantRewardsSection = section.getConfigurationSection("constant-rewards");
        return rewardParser.parseRewardsFromSection(constantRewardsSection);
    }

    private List<String> parseHolographicText(ConfigurationSection section) {
        if (!section.isSet(HOLOGRAPHIC_TEXT)) {
            return Collections.emptyList();
        }

        return section.getStringList(HOLOGRAPHIC_TEXT);
    }

    /**
     * Parses effect configurations from the crate section.
     * Effects are stored as a map of Category to List of effect keys.
     * The effect configurations themselves are registered with EffectServiceComponent.
     *
     * @param section Crate configuration section
     * @param crateName Name of the crate (for logging and effect key generation)
     * @return Map of Category to effect configuration keys
     */
    private Map<Category, List<String>> parseEffects(
            ConfigurationSection section, String crateName) {
        Map<Category, List<String>> effectMap = new HashMap<>();

        if (!section.isSet("effect")) {
            return effectMap;
        }

        ConfigurationSection effectSection = section.getConfigurationSection("effect");
        if (effectSection == null) {
            log.warning(String.format("Crate [%s] has invalid effect section", crateName));
            return effectMap;
        }

        Set<String> effectKeys = effectSection.getKeys(false);
        for (String effectKey : effectKeys) {
            try {
                ConfigurationSection currentEffectSection = effectSection.getConfigurationSection(effectKey);
                if (currentEffectSection == null) {
                    log.warning(String.format(
                            "Crate [%s] has invalid effect configuration for key [%s]", crateName, effectKey));
                    continue;
                }

                // Parse effect category (defaults to OPEN)
                String effectCategoryString = currentEffectSection.getString("category", Category.OPEN.name());
                Category category;
                try {
                    category = Category.valueOf(effectCategoryString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warning(String.format(
                            "Crate [%s] effect [%s] has invalid category [%s], defaulting to OPEN",
                            crateName, effectKey, effectCategoryString));
                    category = Category.OPEN;
                }

                // Store effect key in category map
                effectMap.putIfAbsent(category, new ArrayList<>());
                effectMap.get(category).add(effectKey);

                // Register effect configuration with EffectServiceComponent
                String id = ConfigConstants.generateCrateEffectKeyV2(crateName, category, effectKey);
                plugin.getJavaPluginComponent()
                        .getEffectServiceComponent()
                        .registerEffectConfiguration(id, currentEffectSection);

                log.fine(String.format(
                        "Registered effect [%s] for crate [%s] with category [%s]", effectKey, crateName, category));

            } catch (Exception e) {
                log.warning(String.format(
                        "Failed to parse effect [%s] for crate [%s]: %s", effectKey, crateName, e.getMessage()));
            }
        }

        return effectMap;
    }
}
