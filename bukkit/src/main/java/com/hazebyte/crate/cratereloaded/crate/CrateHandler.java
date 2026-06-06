package com.hazebyte.crate.cratereloaded.crate;

import com.google.common.base.Strings;
import com.hazebyte.crate.api.crate.Crate;
import com.hazebyte.crate.api.crate.CrateRegistrar;
import com.hazebyte.crate.api.crate.CrateType;
import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.api.util.Messenger;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.component.GiveCrateComponent;
import com.hazebyte.crate.cratereloaded.component.OpenCrateComponent;
import com.hazebyte.crate.cratereloaded.component.PluginSettingComponent;
import com.hazebyte.crate.cratereloaded.component.PreviewCrateComponent;
import com.hazebyte.crate.cratereloaded.component.SupplyChestCreateComponent;
import com.hazebyte.crate.cratereloaded.component.model.CrateOpenRequest;
import com.hazebyte.crate.cratereloaded.menu.Size;
import com.hazebyte.crate.cratereloaded.menu.pages.ConfirmationPage;
import com.hazebyte.crate.cratereloaded.menu.pages.CratesPreviewPage;
import com.hazebyte.crate.cratereloaded.model.Config;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import com.hazebyte.crate.cratereloaded.model.RewardImpl;
import com.hazebyte.crate.cratereloaded.model.mapper.CrateMapper;
import com.hazebyte.crate.cratereloaded.parser.YamlCrateV2ParserImpl;
import com.hazebyte.crate.cratereloaded.util.ConfigConstants;
import com.hazebyte.crate.cratereloaded.util.MoreObjects;
import com.hazebyte.crate.cratereloaded.util.PlayerUtil;
import com.hazebyte.crate.cratereloaded.util.item.ItemUtil;
import com.hazebyte.crate.utils.NumberGenerator;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CrateHandler implements CrateRegistrar {

    private static CrateHandler handler;
    private final CorePlugin plugin;
    private final PluginSettingComponent settings;

    // V2 Storage (primary)
    private final Map<String, CrateV2> cratesV2;
    private final Map<String, CrateV2> nameCacheV2;
    private final Map<String, CrateV2> itemCacheV2;

    // Legacy storage (for backwards compatibility with CrateImpl)
    private final List<Crate> crates;
    private final Map<String, Crate> nameCache;
    private final Map<String, Crate> itemCache;

    // This caches the materials for crates.
    // This will prevent checking invalid items.
    private final Set<Material> materialCache;

    // V2 Parser
    private final YamlCrateV2ParserImpl crateV2Parser;
    private final CrateMapper crateMapper;

    private final SupplyChestCreateComponent supplyComponent;
    private final OpenCrateComponent openComponent;
    private final PreviewCrateComponent previewComponent;
    private final GiveCrateComponent giveComponent;

    public CrateHandler(
            CorePlugin plugin,
            SupplyChestCreateComponent supplyComponent,
            OpenCrateComponent openComponent,
            PreviewCrateComponent previewComponent,
            GiveCrateComponent giveComponent,
            PluginSettingComponent settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.supplyComponent = supplyComponent;
        this.openComponent = openComponent;
        this.previewComponent = previewComponent;
        this.giveComponent = giveComponent;
        handler = this;

        // Initialize V2 storage (thread-safe)
        this.cratesV2 = new ConcurrentHashMap<>();
        this.nameCacheV2 = new ConcurrentHashMap<>();
        this.itemCacheV2 = new ConcurrentHashMap<>();

        // Initialize legacy storage (thread-safe for backwards compatibility)
        crates = new CopyOnWriteArrayList<>();
        materialCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
        nameCache = new ConcurrentHashMap<>();
        itemCache = new ConcurrentHashMap<>();

        // Get V2 parser and mapper from DI
        this.crateV2Parser = CorePlugin.getJavaPluginComponent().getYamlCrateV2Parser();
        this.crateMapper = CorePlugin.CRATE_MAPPER;

        readConfigs();
    }

    public static CrateHandler getInstance() {
        return handler;
    }

    private void readConfigs() {
        readCrateParser();
    }

    private void readCrateParser() {
        Collection<Config> crateConfigs = CorePlugin.getJavaPluginComponent()
                .getConfigManagerComponent()
                .getConfigsStartingWith(ConfigConstants.CONFIG_CRATE_STORE_SEARCH_INDEX);

        // Load crates using V2 parser (primary)
        for (Config config : crateConfigs) {
            List<CrateV2> parsedCratesV2 = crateV2Parser.parse(config);
            for (CrateV2 crateV2 : parsedCratesV2) {
                addV2(crateV2);
            }
        }

        // Also maintain legacy storage for backwards compatibility
        // Convert V2 → CrateImpl for existing API consumers
        for (CrateV2 crateV2 : cratesV2.values()) {
            Crate crateImpl = crateMapper.toImplementation(crateV2);
            if (crateImpl != null) {
                this.add(crateImpl);
            }
        }
    }

    @Override
    public Crate createCrate(String name, CrateType type) throws IllegalArgumentException {
        if (Strings.isNullOrEmpty(name) || type == null) {
            throw new IllegalArgumentException("You cannot create a crate with a null name or type");
        }
        return new CrateBuilder(name).setType(type).build();
    }

    @Override
    public Reward createReward() {
        return new RewardImpl();
    }

    @Override
    public Reward createReward(String line) {
        try {
            return new RewardImpl(line);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Crate getCrate(ItemStack item) {
        // Item is null. Likely not a call from PlayerInteractEvent but API
        if (item == null) {
            return null;
        }

        // Material is AIR. No infinite crates.
        if (item.getType() == Material.AIR) {
            return null;
        }

        // If the item's material isn't used as a material
        if (!materialCache.contains(item.getType())) {
            return null;
        }

        String itemKey;
        try {
            itemKey = ItemUtil.toKeyString(item);
        } catch (NullPointerException ignored) {
            return null;
        }
        Crate value = itemCache.get(itemKey);
        if (value != null) {
            return value;
        }

        // Handle custom check level
        if (settings.getCrateComparisonLevel() < 2) {
            for (Crate crate : crates) {
                if (crate.is(item)) {
                    String key = ItemUtil.toKeyString(item);
                    itemCache.put(key, crate);
                }
            }
        }

        return null;
    }

    @Override
    public Crate getCrate(String str) {
        if (str == null || str.equals("")) {
            return null;
        }

        if (nameCache.containsKey(str)) {
            return nameCache.get(str);
        }

        for (Crate crate : crates) {
            if (crate.getCrateName().equalsIgnoreCase(str)) {
                nameCache.put(str, crate);
                return crate;
            }
        }
        return null;
    }

    @Override
    public List<Crate> getCrateFromDisplayName(String str) {
        if (str == null || str.equals("")) {
            return null;
        }

        List<Crate> crateList = new ArrayList<>();
        for (Crate crate : crates) {
            if (crate.getDisplayName().equals(str)) {
                crateList.add(crate);
            }
        }
        return crateList;
    }

    @Override
    public List<Crate> getCrates() {
        return this.crates;
    }

    @Override
    public boolean isCrate(ItemStack item) {
        return getCrate(item) != null;
    }

    @Override
    public void add(Crate crate) {
        if (nameCache.containsKey(crate.getCrateName())) {
            plugin.getLogger().severe(String.format("%s already exists!", crate.getCrateName()));
            return;
        }
        if (crate.getItem() == null) {
            throw new IllegalArgumentException(
                    "Missing activation item.\n" + "Developers: #setItem()\n" + "Users: Please set the item node.");
        }
        if (crate.getCrateName() == null || crate.getCrateName().equals("")) {
            throw new IllegalArgumentException("Missing crate name.\n" + "Invalid Crate Constructor");
        }

        crates.add(crate);
        materialCache.add(crate.getItem().getType());
        nameCache.put(crate.getCrateName(), crate);
        itemCache.put(ItemUtil.toKeyString(crate.getItem()), crate);
    }

    @Override
    public void remove(Crate crate) {
        crates.remove(crate);
        // The item may be changed if it's via an API.
        materialCache.remove(crate.getItem().getType());
        nameCache.remove(crate.getCrateName());
        itemCache.remove(ItemUtil.toKeyString(crate.getItem()));
        // Also remove from V2 storage
        cratesV2.remove(crate.getCrateName());
        nameCacheV2.remove(crate.getCrateName());
    }

    /**
     * Adds a CrateV2 to the V2 storage.
     * This is the primary storage method for V2 migration.
     */
    public void addV2(CrateV2 crateV2) {
        if (cratesV2.containsKey(crateV2.getCrateName())) {
            plugin.getLogger().severe(String.format("%s already exists!", crateV2.getCrateName()));
            return;
        }
        if (crateV2.getItem() == null) {
            throw new IllegalArgumentException(
                    "Missing activation item in CrateV2: " + crateV2.getCrateName());
        }

        // Add to V2 storage
        cratesV2.put(crateV2.getCrateName(), crateV2);
        nameCacheV2.put(crateV2.getCrateName(), crateV2);
        itemCacheV2.put(ItemUtil.toKeyString(crateV2.getItem()), crateV2);
        materialCache.add(crateV2.getItem().getType());
    }

    /**
     * Gets a CrateV2 by name.
     * This is the primary accessor for V2 migration.
     */
    public CrateV2 getCrateV2(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return nameCacheV2.get(name);
    }

    /**
     * Gets all CrateV2 objects.
     * This is the primary accessor for V2 migration.
     */
    public Collection<CrateV2> getCratesV2() {
        return cratesV2.values();
    }

    @Override
    public List<Reward> generateCrateRewards(Crate crate, Player player) {
        int numberOfPrizes = getNumberOfPrizes(crate);
        return generateCrateRewards(crate, player, numberOfPrizes, false, true);
    }

    public List<Reward> generateCrateRewards(
            Crate crate, Player player, int amountOfPrizes, boolean overrideUniqueRule, boolean addConstantRewards) {
        return CorePlugin.getJavaPluginComponent()
                .getGenerateCratePrizeComponent()
                .generateCratePrize(crate, player, amountOfPrizes, overrideUniqueRule, addConstantRewards);
    }

    private int getNumberOfPrizes(Crate crate) {
        int min = crate.getMinimumRewards();
        int max = Math.min(crate.getMaximumRewards(), crate.getRewards().size());
        int numberOfPrizes = Math.max(1, NumberGenerator.range(min, max));
        return numberOfPrizes;
    }

    @Override
    public void open(Crate crate, Player player, Location location) {
        open(crate, player, location, new HashMap<>());
    }

    @Override
    public void open(Crate crate, Player player, Location location, Map<String, Object> settings) {
        String logMessage = String.format("Player [%s] is opening crate [%s]", player.getName(), crate.getCrateName());
        plugin.getLogger().finer(logMessage);

        if (this.generateCrateRewards(crate, player).isEmpty()) {
            Messenger.tell(player, plugin.getMessage("core.invalid_crate_reward"));
            return;
        }

        ItemStack itemToRemove = getObjectFromSetting(settings, "itemToRemove", PlayerUtil.getItemInHand(player));
        Boolean shouldRemoveItem = getObjectFromSetting(settings, "shouldRemoveItem", Boolean.TRUE);

        // Get CrateV2 from storage (primary)
        CrateV2 crateV2 = getCrateV2(crate.getCrateName());

        CrateOpenRequest request = CrateOpenRequest.builder()
                .player(player)
                .location(location)
                .crate(crate)           // Legacy (for backwards compatibility)
                .crateV2(crateV2)       // V2 (primary)
                .build();
        if (crate.getType() == CrateType.SUPPLY) {
            supplyComponent.createChest(request);
        } else {
            openComponent.openCrate(request);
        }

        if (shouldRemoveItem) {
            if (player.getGameMode() == GameMode.CREATIVE && crate.getType() == CrateType.KEY) {
                PlayerUtil.removeOneFromHandOrInv(player, itemToRemove);
            } else {
                PlayerUtil.removeOneFromHand(player);
            }
        }
    }

    @Override
    public void openConfirmationPage(Crate crate, Player player, Location location) {
        ConfirmationPage page = new ConfirmationPage(
                crate,
                location,
                MoreObjects.firstNonNull(crate.getDisplayItem(), PlayerUtil.getItemInHand(player)),
                true,
                settings);
        page.open(player);
    }

    private <T> T getObjectFromSetting(Map<String, Object> map, String key, T defaultValue) {
        return (T) map.getOrDefault(key, defaultValue);
    }

    @Override
    public void preview(Crate crate, Player player) {
        previewComponent.previewCrate(crate, player);
    }

    @Override
    public void previewAll(List<Crate> crates, Player player) {
        if (MoreObjects.isNullOrEmpty(crates)) {
            return;
        }

        crates = crates.stream().filter(Crate::isPreviewable).collect(Collectors.toList());
        if (!crates.isEmpty()) {
            if (crates.size() == 1) {
                preview(crates.get(0), player);
            } else {
                int size = crates.size()
                        + (settings.isMenuInteractionEnabled()
                                ? Size.ONE_LINE.getSize()
                                : 0);
                CratesPreviewPage page = new CratesPreviewPage(crates, Size.fit(size), settings);
                page.open(player);
            }
        }
    }

    @Override
    public boolean purchase(Crate crate, Player player, int amount) {
        double cost = amount * crate.getCost();
        boolean success = plugin.getEconomyProvider()
                .withdraw(player, cost)
                .transactionSuccess();
        if (success) {
            giveCrate(crate, player, amount);
            return true;
        }
        return false;
    }

    @Override
    public void giveCrate(Crate crate, Player player, int amount) {
        giveComponent.giveCrateToOnlinePlayer(player, crate, amount);
    }

    @Override
    public String getCrateString() {
        if (crates == null) {
            return "";
        }

        StringJoiner builder = new StringJoiner(", ");
        for (Crate crate : crates) {
            builder.add(crate.getCrateName());
        }
        return builder.toString();
    }
}
