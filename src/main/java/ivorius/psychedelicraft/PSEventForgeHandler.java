/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft;

import ivorius.psychedelicraft.fluids.AlcoholicFluid;
import ivorius.psychedelicraft.items.FluidContainerItem;
import net.minecraft.item.ItemStack;

/**
 * Created by lukas on 18.02.14.
 */
@Deprecated(forRemoval = true)
public class PSEventForgeHandler
{
    public static boolean containsAlcohol(ItemStack stack, AlcoholicFluid fluid, Boolean distilled, int minMatured)
    {
        return stack.getItem() instanceof FluidContainerItem container
                && container.getFluid(stack) == fluid
                && (distilled == null || (fluid.getDistillation(stack) > 0) == distilled)
                && fluid.getMaturation(stack) >= minMatured;
    }

    // TODO: (Sollace) reimplement chat distortion

    //@SubscribeEvent
    /*
    public void onServerChat(ServerChatEvent event)
    {
        if (PSConfig.distortOutgoingMessages)
        {
            Object[] args = event.component.getFormatArgs();

            if (args.length >= 2 && args[1] instanceof ChatComponentText)
            {
                DrugProperties drugProperties = DrugProperties.getDrugProperties(event.player);

                if (drugProperties != null)
                {
                    String message = event.message;
                    String modified = drugProperties.messageDistorter.distortOutgoingMessage(drugProperties, event.player, event.player.getRNG(), message);
                    if (!modified.equals(message))
                        args[1] = ForgeHooks.newChatWithLinks(modified); // See NetHandlerPlayServer
                }
            }
            else
            {
                Psychedelicraft.logger.warn("Failed distorting outgoing text message! Args: " + Arrays.toString(args));
            }
        }
    }*/

//    @SubscribeEvent
//    public void onClientChatReceived(ClientChatReceivedEvent event)
//    {
//        // Doesn't work, but is not used yet anyway
//        if (PSConfig.distortIncomingMessages && event.message instanceof ChatComponentText)
//        {
//            ChatComponentText text = (ChatComponentText) event.message;
//
//            EntityLivingBase renderEntity = Minecraft.getMinecraft().renderViewEntity;
//            DrugProperties drugProperties = DrugProperties.getDrugProperties(renderEntity);
//
//            if (drugProperties != null)
//            {
//                String message = text.getUnformattedTextForChat();
//                drugProperties.receiveChatMessage(renderEntity, message);
//                String modified = drugProperties.messageDistorter.distortIncomingMessage(drugProperties, renderEntity, renderEntity.getRNG(), message);
//
//                event.message = new ChatComponentText(modified);
//            }
//        }
//    }
/*
    //@SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event)
    {
        DrugProperties drugProperties = DrugProperties.getDrugProperties(event.entityLiving);

        if (drugProperties != null)
        {
            EntityPlayer.EnumStatus status = drugProperties.getDrugSleepStatus();

            if (status != null)
                event.result = status;
        }
    }
*/
}
