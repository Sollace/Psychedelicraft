package ivorius.psychedelicraft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.ColorHelper;

public class SmokeColors {
    public static final int WHITE = 0xFF99997F/*ColorHelper.getArgb(153, 153, 127)*/;
    public static final int GREY = 0xFFE5E5E5/*ColorHelper.getArgb(229, 229, 229)*/;
    public static final int GREEN = 0xFF7FE566/*ColorHelper.getArgb(127, 229, 102)*/;

    public static final Codec<Integer> COLOR_COMPONENT_CODEC = Codec.FLOAT.xmap(ColorHelper::channelFromFloat, i -> i / 255F);
    public static final Codec<Integer> CODEC = RecordCodecBuilder.create(i -> i.group(
            SmokeColors.COLOR_COMPONENT_CODEC.fieldOf("r").forGetter(ColorHelper::getRed),
            SmokeColors.COLOR_COMPONENT_CODEC.fieldOf("g").forGetter(ColorHelper::getGreen),
            SmokeColors.COLOR_COMPONENT_CODEC.fieldOf("b").forGetter(ColorHelper::getBlue)
    ).apply(i, ColorHelper::getArgb));
}
