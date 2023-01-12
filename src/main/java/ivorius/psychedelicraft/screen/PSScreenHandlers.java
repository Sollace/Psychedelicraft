package ivorius.psychedelicraft.screen;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.entity.*;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

/**
 * @author Sollace
 * @since 12 Jan 2023
 */
public interface PSScreenHandlers {
    ScreenHandlerType<DryingTableScreenHandler> DRYING_TABLE = register("drying_table", new ExtendedScreenHandlerType<>(DryingTableScreenHandler::new));

    ScreenHandlerType<FluidContraptionScreenHandler<BarrelBlockEntity>> BARREL = register("barrel", new ExtendedScreenHandlerType<>(
            (sync, inventory, buf) -> new FluidContraptionScreenHandler<>(PSScreenHandlers.BARREL, sync, inventory, buf)
    ));
    ScreenHandlerType<FluidContraptionScreenHandler<DistilleryBlockEntity>> DISTILLERY = register("distillery", new ExtendedScreenHandlerType<>(
            (sync, inventory, buf) -> new FluidContraptionScreenHandler<>(PSScreenHandlers.DISTILLERY, sync, inventory, buf)
    ));
    ScreenHandlerType<FluidContraptionScreenHandler<FlaskBlockEntity>> FLASK = register("flask", new ExtendedScreenHandlerType<>(
            (sync, inventory, buf) -> new FluidContraptionScreenHandler<>(PSScreenHandlers.FLASK, sync, inventory, buf)
    ));
    ScreenHandlerType<FluidContraptionScreenHandler<MashTubBlockEntity>> MASH_TUB = register("mash_tub", new ExtendedScreenHandlerType<>(
            (sync, inventory, buf) -> new FluidContraptionScreenHandler<>(PSScreenHandlers.MASH_TUB, sync, inventory, buf)
    ));

    static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, ScreenHandlerType<T> type) {
        return Registry.register(Registries.SCREEN_HANDLER, Psychedelicraft.id(name), type);
    }

    static void bootstrap() { }
}
