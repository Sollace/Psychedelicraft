package ivorius.psychedelicraft.item.component;

import java.util.function.Consumer;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

public interface TooltipAppender {
    void appendTooltip(TooltipContext context, Consumer<Text> tooltip);
}
