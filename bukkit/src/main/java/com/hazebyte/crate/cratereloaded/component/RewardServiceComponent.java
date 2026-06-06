package com.hazebyte.crate.cratereloaded.component;

import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.cratereloaded.model.RewardV2;
import java.util.List;
import java.util.function.Predicate;

public interface RewardServiceComponent {

    List<Reward> createPrizePool(List<Reward> rewards, List<Predicate<Reward>> rules);

    Reward generatePrize(List<Reward> rewards);

    List<RewardV2> createPrizePoolV2(List<RewardV2> rewards, List<Predicate<RewardV2>> rules);

    RewardV2 generatePrizeV2(List<RewardV2> rewards);
}
