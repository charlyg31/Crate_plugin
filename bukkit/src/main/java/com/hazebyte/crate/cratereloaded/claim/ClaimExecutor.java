package com.hazebyte.crate.cratereloaded.claim;

import com.hazebyte.crate.api.CrateAPI;
import com.hazebyte.crate.api.claim.Claim;
import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.api.event.ClaimEvent;
import com.hazebyte.crate.api.util.Messenger;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.component.OpenCrateComponent;
import com.hazebyte.crate.cratereloaded.util.PlayerUtil;
import com.hazebyte.crate.cratereloaded.util.format.CustomFormat;
import com.hazebyte.crate.logger.JSONLogRecord;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ClaimExecutor implements Function<Claim, Boolean> {

    private final CorePlugin plugin;
    private final OpenCrateComponent openCrateComponent;
    private final Logger logger;

    public ClaimExecutor(
            CorePlugin plugin,
            OpenCrateComponent openCrateComponent,
            Logger logger) {
        this.plugin = plugin;
        this.openCrateComponent = openCrateComponent;
        this.logger = logger;
    }

    protected boolean isValidEvent(ClaimEvent event) {
        if (event == null) {
            return false;
        }

        if (event.getClaim() == null) {
            return false;
        }

        if (event.getClaim().getOwner() == null) {
            return false;
        }

        if (event.getClaim().getRewards() == null
                || event.getClaim().getRewards().size() == 0) {
            return false;
        }

        return !event.isCancelled();
    }

    protected boolean isValidExecution(ClaimEvent event) {
        Claim claim = event.getClaim();
        Player player = claim.getOwner().getPlayer();

        if (!claim.getOwner().isOnline()) {
            return false;
        }

        if (PlayerUtil.isInventoryFull(player)) {
            String message = CrateAPI.getMessage("core.claim_inventory_full");
            Messenger.tell(player, CustomFormat.format(message, player));
            return false;
        }

        return true;
    }

    protected void executeEvent(ClaimEvent event) {
        Player player = event.getClaim().getOwner().getPlayer();
        for (Reward reward : event.getClaim().getRewards()) {
            openCrateComponent.executeReward(player, reward);
        }

        String message = CustomFormat.format(
                CrateAPI.getMessage("core.claim_successful"),
                event.getClaim().getRewards().get(0));
        Messenger.tell(player, message);
    }

    protected void logEvent(ClaimEvent event) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonRewards = new JSONArray();
        event.getClaim().getRewards().stream().forEach(reward -> jsonRewards.add(reward.toString()));
        jsonObject.put("player", event.getClaim().getOwner().getPlayer().getName());
        jsonObject.put("rewards", jsonRewards);

        JSONLogRecord record = new JSONLogRecord(Level.FINE, "claim", jsonObject);
        logger.log(record);
    }

    @Override
    public Boolean apply(Claim claim) {
        ClaimEvent event = new ClaimEvent(claim);
        Bukkit.getPluginManager().callEvent(event);

        if (!isValidEvent(event)) {
            return false;
        }

        if (!isValidExecution(event)) {
            return false;
        }

        executeEvent(event);
        logEvent(event);

        return true;
    }
}
