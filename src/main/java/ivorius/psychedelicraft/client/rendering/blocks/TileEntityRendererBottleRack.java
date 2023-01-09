package ivorius.psychedelicraft.client.rendering.blocks;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.TileEntityBottleRack;
import ivorius.psychedelicraft.fluids.SimpleFluid;
import ivorius.psychedelicraft.items.FluidContainerItem;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 6 Jan 2023
 */
public class TileEntityRendererBottleRack implements BlockEntityRenderer<TileEntityBottleRack> {
    public static final Identifier TEXTURE = Psychedelicraft.id(Psychedelicraft.TEXTURES_PATH + "wineRack.png");

    public Model model;// = AdvancedModelLoader.loadModel(Psychedelicraft.id(Psychedelicraft.filePathModels + "wineRack.obj"));

    public TileEntityRendererBottleRack(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(TileEntityBottleRack entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        //IvRotatableBlockRenderHelper.transformFor(tileEntityBottleRack, x, y, z);
        matrices.translate(0, 0.002F, 0);

        matrices.push();
        matrices.translate(0, -0.5F, 0);
        model.render(matrices, vertices.getBuffer(model.getLayer(TEXTURE)), light, overlay, 1, 1, 1, 1);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (int i = 0; i < entity.size(); i++) {
            ItemStack bottle = entity.getStack(i);
            if (!bottle.isEmpty()) {
                int fluidColor = -1;
                int dyeColor = -1;
                if (bottle.getItem() instanceof FluidContainerItem container) {
                    SimpleFluid fluid = container.getFluid(bottle);
                    fluidColor = fluid.getTranslucentColor(bottle);
                }
                if (bottle.getItem() instanceof DyeableItem dyeable) {
                    dyeColor = dyeable.getColor(bottle);
                }

                int color = fluidColor == -1 || dyeColor == -1 ? (fluidColor == -1 ? dyeColor : fluidColor) : MathUtils.mixColors(fluidColor, dyeColor, 0.5F);
                if (color != -1) {
                    RenderSystem.setShaderColor(MathUtils.r(color), MathUtils.g(color), MathUtils.b(color), 1);
                }
                // TODO: Convert from an obj model
                // model.renderOnly("Bottle_" + i);
            }
        }
        matrices.pop();
        RenderSystem.disableBlend();

        matrices.pop();
    }
}
