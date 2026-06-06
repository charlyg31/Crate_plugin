package com.hazebyte.crate.cratereloaded.model.menuV2;

import static com.hazebyte.crate.cratereloaded.model.menuV2.InventoryButtonsV2.createListCrateRewardsAdminPageButton;
import static com.hazebyte.crate.cratereloaded.util.InventoryConstants.MAX_INVENTORY_SIZE_WITH_NAV;
import static com.hazebyte.crate.cratereloaded.util.InventoryConstants.SIX_ROWS;
import static com.hazebyte.crate.cratereloaded.util.InventoryConstants.THREE_ROWS;
import static com.hazebyte.crate.cratereloaded.util.InventoryConstants.getValidatedTitle;

import com.hazebyte.crate.cratereloaded.menuV2.InventoryButtonV2;
import com.hazebyte.crate.cratereloaded.menuV2.InventoryV2;
import com.hazebyte.crate.cratereloaded.model.CrateV2;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InventoryMenusV2 extends InventoryV2 {

    public static <T extends InventoryButtonV2> Map<Integer, InventoryButtonV2> toMap(List<T> list) {
        return IntStream.range(0, list.size()).boxed().collect(Collectors.toMap(i -> i, i -> list.get(i)));
    }

    public static InventoryV2 getCrateAdminMenu(CrateV2 crateV2) {
        return InventoryV2.builder()
                .title(getValidatedTitle(crateV2.getCrateName()))
                .inventorySize(THREE_ROWS)
                .buttons(new HashMap<Integer, InventoryButtonV2>() {
                    {
                        put(10, createListCrateRewardsAdminPageButton(crateV2));
                        put(12, InventoryButtonsV2.createNotImplementedButton());
                        put(14, InventoryButtonsV2.createNotImplementedButton());
                        put(16, InventoryButtonsV2.createNotImplementedButton());
                    }
                })
                .build();
    }

    public static InventoryV2 getListCratesAdminMenu(List<CrateV2> crateV2) {
        Map<Integer, InventoryButtonV2> buttons = toMap(crateV2.stream()
                .map(InventoryButtonsV2::createCrateAdminPageButton)
                .limit(MAX_INVENTORY_SIZE_WITH_NAV)
                .collect(Collectors.toList()));

        return InventoryV2.builder()
                .title(getValidatedTitle("Crates"))
                .inventorySize(SIX_ROWS)
                .buttons(buttons)
                .build();
    }

    public static InventoryV2 getListCrateRewardsMenu(CrateV2 crateV2) {
        Map<Integer, InventoryButtonV2> buttons = toMap(crateV2.getRewards().stream()
                .limit(MAX_INVENTORY_SIZE_WITH_NAV)
                .map(InventoryButtonsV2::createExecuteRewardButton)
                .collect(Collectors.toList()));
        return InventoryV2.builder()
                .title(getValidatedTitle(crateV2.getCrateName()))
                .inventorySize(SIX_ROWS)
                .buttons(buttons)
                .build();
    }
}
