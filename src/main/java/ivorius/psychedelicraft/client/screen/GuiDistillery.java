package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.*;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lukas on 13.11.14.
 */
public class GuiDistillery extends GuiFluidHandler
{
    public static final Identifier distilleryTexture = Psychedelicraft.id(Psychedelicraft.filePathTextures + "container_distillery.png");

    private DistilleryBlockEntity tileEntityDistillery;

    public GuiDistillery(InventoryPlayer inventoryPlayer, DistilleryBlockEntity tileEntity)
    {
        super(inventoryPlayer, tileEntity, tileEntity, ForgeDirection.DOWN);
        this.tileEntityDistillery = tileEntity;
    }

    @Override
    protected Identifier getBackgroundTexture()
    {
        return distilleryTexture;
    }

    @Override
    protected void drawAdditionalInfo(int baseX, int baseY)
    {
        int timeLeftDistilling = tileEntityDistillery.getRemainingDistillationTimeScaled(13);
        if (timeLeftDistilling < 13)
            drawTexturedModalRect(baseX + 24, baseY + 15 + timeLeftDistilling, 176, timeLeftDistilling, 20, 13 - timeLeftDistilling);
    }


    @Override
    protected List<String> getAdditionalTankText()
    {
        return tileEntityDistillery.isDistilling() ? Arrays.asList(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("fluid.status.distilling")) : null;
    }
}
