package com.hazebyte.crate.cratereloaded.component.model;

import com.hazebyte.crate.api.crate.Crate;
import com.hazebyte.crate.cratereloaded.CorePlugin;
import com.hazebyte.crate.cratereloaded.model.CrateImpl;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CrateOpenRequest {
    private Player player;
    private Location location;
    private Crate crate;
    private CrateV2 crateV2;

    private CrateOpenRequest() {}

    public CrateOpenRequest(Player player, Location location, Crate crate, CrateV2 crateV2) {
        this.player = player;
        this.location = location;
        this.crate = crate;
        this.crateV2 = crateV2;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Player player;
        private Location location;
        private Crate crate;
        private CrateV2 crateV2;

        public Builder player(Player player) { this.player = player; return this; }
        public Builder location(Location location) { this.location = location; return this; }
        public Builder crate(Crate crate) { this.crate = crate; return this; }
        public Builder crateV2(CrateV2 crateV2) { this.crateV2 = crateV2; return this; }
        public CrateOpenRequest build() { return new CrateOpenRequest(player, location, crate, crateV2); }
    }

    public Player getPlayer() { return player; }
    public Location getLocation() { return location; }
    public Crate getCrate() { return crate; }
    public CrateV2 getCrateV2() { return crateV2; }
    public void setPlayer(Player player) { this.player = player; }
    public void setLocation(Location location) { this.location = location; }
    public void setCrate(Crate crate) { this.crate = crate; }
    public void setCrateV2(CrateV2 crateV2) { this.crateV2 = crateV2; }

    public CrateV2 getCrateV2OrConvert() {
        if (crateV2 != null) return crateV2;
        if (crate != null) return CorePlugin.CRATE_MAPPER.fromImplementation((CrateImpl) crate);
        throw new IllegalStateException("CrateOpenRequest must have either crate or crateV2 set");
    }

    public Crate getCrateOrConvert() {
        if (crate != null) return crate;
        if (crateV2 != null) return CorePlugin.CRATE_MAPPER.toImplementation(crateV2);
        throw new IllegalStateException("CrateOpenRequest must have either crate or crateV2 set");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrateOpenRequest)) return false;
        CrateOpenRequest that = (CrateOpenRequest) o;
        return java.util.Objects.equals(player, that.player) &&
               java.util.Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(player, location); }

    @Override
    public String toString() { return "CrateOpenRequest(player=" + player + ")"; }
}
