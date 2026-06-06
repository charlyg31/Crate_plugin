package com.hazebyte.crate.cratereloaded.component.impl;

import com.hazebyte.crate.api.crate.reward.Reward;
import com.hazebyte.crate.cratereloaded.component.RewardServiceComponent;
import com.hazebyte.crate.cratereloaded.model.RewardV2;
import com.hazebyte.utils.collection.WeightedCollection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RewardServiceComponentImpl implements RewardServiceComponent {

    @Override
    public List<Reward> createPrizePool(List<Reward> rewards, List<Predicate<Reward>> rules) {
        return applyPredicateOnList(rewards, rules);
    }

    @Override
    public Reward generatePrize(List<Reward> prizePool) {
        WeightedCollection<Reward> rewards = new WeightedCollection<>();
        for (Reward item : prizePool) {
            rewards.add(item.getChance(), item);
        }
        return rewards.next();
    }

    @Override
    public List<RewardV2> createPrizePoolV2(List<RewardV2> rewards, List<Predicate<RewardV2>> rules) {
        return applyPredicateOnList(rewards, rules);
    }

    @Override
    public RewardV2 generatePrizeV2(List<RewardV2> rewards) {
        WeightedCollection<RewardV2> rewardsV2 = new WeightedCollection<>();
        for (RewardV2 item : rewards) {
            rewardsV2.add(item.getChance(), item);
        }
        return rewardsV2.next();
    }

    private <T> List<T> applyPredicateOnList(List<T> list, List<Predicate<T>> rules) {
        return list.stream().filter(item -> allPredicateMatch(item, rules)).collect(Collectors.toList());
    }

    private <T> boolean allPredicateMatch(T item, List<Predicate<T>> predicates) {
        return predicates.stream().map(predicate -> predicate.test(item)).allMatch(a -> a);
    }
}
