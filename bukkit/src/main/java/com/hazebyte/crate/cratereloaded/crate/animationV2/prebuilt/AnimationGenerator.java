package com.hazebyte.crate.cratereloaded.crate.animationV2.prebuilt;

import com.hazebyte.crate.cratereloaded.crate.animationV2.Animation;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import org.bukkit.entity.Player;

public interface AnimationGenerator {

    
    Animation createAnimation(Player player, CrateV2 crateV2);
}
