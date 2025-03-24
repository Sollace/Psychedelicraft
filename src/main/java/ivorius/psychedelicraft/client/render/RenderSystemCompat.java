package ivorius.psychedelicraft.client.render;

import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;

@Deprecated
public interface RenderSystemCompat {
    AtomicBoolean COMPAT_ENABLED = new AtomicBoolean();

    static void disableBlend() {
        if (COMPAT_ENABLED.get()) {
            GlStateManager._disableBlend();
        }
    }

    static void enableBlend() {
        if (COMPAT_ENABLED.get()) {
            GlStateManager._enableBlend();
        }
    }

    static void disableDepthTest() {
        if (COMPAT_ENABLED.get()) {
            GlStateManager._disableDepthTest();
        }
    }

    static void enableDepthTest() {
        if (COMPAT_ENABLED.get()) {
            GlStateManager._enableDepthTest();
        }
    }

    static void depthMask(boolean mask) {
        if (COMPAT_ENABLED.get()) {
            GlStateManager._depthMask(mask);
        }
    }

    static void blendFuncSeparate(BlendFunction blendFunc) {
        if (COMPAT_ENABLED.get()) {
            GlStateManager._enableBlend();
            GlStateManager._blendFuncSeparate(GlConst.toGl(blendFunc.sourceColor()), GlConst.toGl(blendFunc.destColor()), GlConst.toGl(blendFunc.sourceAlpha()), GlConst.toGl(blendFunc.destAlpha()));
        }
    }

    static void blendFuncSeparate(SourceFactor sourceA, DestFactor destA, SourceFactor sourceB, DestFactor destB) {
        if (COMPAT_ENABLED.get()) {
            GlStateManager._enableBlend();
            GlStateManager._blendFuncSeparate(GlConst.toGl(sourceA), GlConst.toGl(destA), GlConst.toGl(sourceB), GlConst.toGl(destB));
        }
    }
}
