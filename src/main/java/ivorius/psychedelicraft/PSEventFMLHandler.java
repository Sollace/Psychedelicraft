/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Created by lukas on 18.02.14.
 */
@Deprecated(forRemoval = true)
public class PSEventFMLHandler {
    // TODO: (Sollace) need to hook a tick event.
    //@SubscribeEvent
    public void onPlayerTick(/*TickEvent.PlayerTickEvent event*/ PlayerEntity player) {
        //if (event.phase == TickEvent.Phase.END)
        //{
            DrugProperties drugProperties = DrugProperties.getDrugProperties(player);

            if (drugProperties != null) {
                drugProperties.updateDrugEffects(player);
            }
        //}
    }

 // TODO: (Sollace) Probably not needed any more
    /*
    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
    {
        if (event.craftMatrix instanceof InventoryCrafting)
            RecipeActionRegistry.finalizeCrafting(event.crafting, (InventoryCrafting) event.craftMatrix, event.player);
    }*/
}
