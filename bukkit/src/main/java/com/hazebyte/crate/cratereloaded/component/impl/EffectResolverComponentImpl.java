package com.hazebyte.crate.cratereloaded.component.impl;

import com.hazebyte.crate.api.crate.Crate;
import com.hazebyte.crate.api.effect.Category;
import com.hazebyte.crate.cratereloaded.component.EffectResolverComponent;
import com.hazebyte.crate.cratereloaded.component.EffectServiceComponent;
import com.hazebyte.crate.cratereloaded.model.CrateImpl;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import com.hazebyte.crate.cratereloaded.provider.effect.EffectWrapper;
import com.hazebyte.crate.cratereloaded.util.ConfigConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Implementation of EffectResolverComponent that resolves effect configurations
 * into executable effect wrappers for both CrateV2 and CrateImpl.
 */
@Singleton
public class EffectResolverComponentImpl implements EffectResolverComponent {

    private final EffectServiceComponent effectService;

    @Inject
    public EffectResolverComponentImpl(EffectServiceComponent effectService) {
        this.effectService = effectService;
    }

    @Override
    public List<EffectWrapper> getEffects(Crate crate, Category category) {
        if (crate instanceof CrateV2) {
            return getEffectsV2((CrateV2) crate, category);
        } else if (crate instanceof CrateImpl) {
            return getEffectsLegacy((CrateImpl) crate, category);
        }
        return Collections.emptyList();
    }

    @Override
    public List<EffectWrapper> getEffectsV2(CrateV2 crate, Category category) {
        List<EffectWrapper> effects = new ArrayList<>();
        Map<Category, List<String>> effectMap = crate.getEffectCategoryToId();

        if (!effectMap.containsKey(category)) {
            return effects;
        }

        List<String> effectKeys = effectMap.get(category);
        for (String effectKey : effectKeys) {
            // Generate effect configuration ID using crate name
            String id = ConfigConstants.generateCrateEffectKeyV2(crate.getCrateName(), category, effectKey);

            // Retrieve effect configuration from EffectServiceComponent
            Optional<ConfigurationSection> configOpt = effectService.getEffectConfiguration(id);
            if (!configOpt.isPresent()) {
                continue;
            }

            // Create effect wrapper from configuration
            Optional<? extends EffectWrapper> wrapperOpt = effectService.createEffect(configOpt.get());
            if (!wrapperOpt.isPresent()) {
                continue;
            }

            effects.add(wrapperOpt.get());
        }

        return effects;
    }

    @Override
    public List<EffectWrapper> getEffectsLegacy(CrateImpl crate, Category category) {
        // Delegate to existing CrateImpl.getEffects() for backward compatibility
        return crate.getEffects(category);
    }
}
