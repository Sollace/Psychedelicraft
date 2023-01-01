package ivorius.psychedelicraft.client.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.blocks.TileEntityBarrel;
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
public class GuiBarrel extends GuiFluidHandler
{
    public static final ResourceLocation barrelTexture = new ResourceLocation(Psychedelicraft.MODID, Psychedelicraft.filePathTextures + "container_barrel.png");

    private TileEntityBarrel tileEntityBarrel;

    public GuiBarrel(InventoryPlayer inventoryPlayer, TileEntityBarrel tileEntity)
    {
        super(inventoryPlayer, tileEntity, tileEntity, ForgeDirection.DOWN);
        this.tileEntityBarrel = tileEntity;
    }

    @Override
    protected ResourceLocation getBackgroundTexture()
    {
        return barrelTexture;
    }

    @Override
    protected void drawAdditionalInfo(int baseX, int baseY)
    {
        int timeLeftFermenting = tileEntityBarrel.getRemainingFermentationTimeScaled(24);
        if (timeLeftFermenting < 24)
            drawTexturedModalRect(baseX + 23, baseY + 14, 176, 0, 24 - timeLeftFermenting, 17);
    }

    @Override
    protected List<String> getAdditionalTankText()
    {
        return tileEntityBarrel.isFermenting() ? Arrays.asList(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("fluid.status.maturing")) : null;
    }
}
