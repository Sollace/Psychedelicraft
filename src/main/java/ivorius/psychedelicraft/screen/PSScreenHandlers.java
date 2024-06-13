package ivorius.psychedelicraft.screen;

import java.util.concurrent.atomic.AtomicReference;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.entity.*;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;

/**
 * @author Sollace
 * @since 12 Jan 2023
 */
public interface PSScreenHandlers {
    ScreenHandlerType<DryingTableScreenHandler> DRYING_TABLE = register("drying_table", new ExtendedScreenHandlerType<>(DryingTableScreenHandler::new, BlockPos.PACKET_CODEC));

    ScreenHandlerType<FluidContraptionScreenHandler<BarrelBlockEntity>> BARREL = register("barrel", contraptionScreenHander());
    ScreenHandlerType<FluidContraptionScreenHandler<DistilleryBlockEntity>> DISTILLERY = register("distillery", contraptionScreenHander());
    ScreenHandlerType<FluidContraptionScreenHandler<FlaskBlockEntity>> FLASK = register("flask", contraptionScreenHander());
    ScreenHandlerType<FluidContraptionScreenHandler<MashTubBlockEntity>> MASH_TUB = register("mash_tub", contraptionScreenHander());

    static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, ScreenHandlerType<T> type) {
        return Registry.register(Registries.SCREEN_HANDLER, Psychedelicraft.id(name), type);
    }

    static <T extends FlaskBlockEntity> ScreenHandlerType<FluidContraptionScreenHandler<T>> contraptionScreenHander() {
        final AtomicReference<ScreenHandlerType<FluidContraptionScreenHandler<T>>> type = new AtomicReference<>(null);
        type.set(new ExtendedScreenHandlerType<>(
                (sync, inventory, data) -> new FluidContraptionScreenHandler<>(type.get(), sync, inventory, data),
                BlockWithFluid.InteractionData.PACKET_CODEC
        ));
        return type.get();
    }

    static void bootstrap() { }
}
