package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 13.11.14.
 */
public class GuiWoodenVat extends GuiFluidHandler
{
    public static final ResourceLocation woodenVatTexture = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "container_woodenVat.png");

    private MashTubBlockEntity tileEntityMashTub;

    public GuiWoodenVat(InventoryPlayer inventoryPlayer, MashTubBlockEntity tileEntity)
    {
        super(inventoryPlayer, tileEntity, tileEntity, ForgeDirection.DOWN);
        this.tileEntityMashTub = tileEntity;
    }

    @Override
    protected ResourceLocation getBackgroundTexture()
    {
        return woodenVatTexture;
    }

    @Override
    protected void drawAdditionalInfo(int baseX, int baseY)
    {
        int timeLeftFermenting = tileEntityMashTub.getRemainingFermentationTimeScaled(24);
        if (timeLeftFermenting < 24)
            drawTexturedModalRect(baseX + 23, baseY + 14, 176, 0, 24 - timeLeftFermenting, 17);
    }


    @Override
    protected List<String> getAdditionalTankText()
    {
        return tileEntityMashTub.isFermenting() ? Arrays.asList(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("fluid.status.fermenting")) : null;
    }
}
