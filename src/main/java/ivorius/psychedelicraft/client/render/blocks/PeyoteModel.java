/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */
package ivorius.psychedelicraft.client.render.blocks;

import net.minecraft.client.model.*;

interface PeyoteModel {
    static TexturedModelData stage0() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("one", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-1F, -1.5F, -1F, 2, 2, 2), ModelTransform.of(0F, 24F, 0F, 0.0371786F, 0.2230717F, 0F));
        return TexturedModelData.of(data, 32, 32);
    }

    static TexturedModelData stage1() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("one", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-2F, -2F, -2F, 3, 3, 3), ModelTransform.of(0F, 24F, 0, 0.0371786F, 0.1487144F, 0F));
        root.addChild("two", ModelPartBuilder.create().uv(0, 7).mirrored().cuboid(0F, -1F, -1.5F, 2, 2, 2), ModelTransform.of(1F, 24F, 0F, 0.1858931F, -0.2230717F, 0F));
        root.addChild("three", ModelPartBuilder.create().uv(0, 12).mirrored().cuboid(0F, -1.5F, 0F, 2, 2, 2), ModelTransform.of(0F, 24F, 1F, -0.2230717F, 0.3346075F, 0F));
        root.addChild("four", ModelPartBuilder.create().uv(0, 17).mirrored().cuboid(1F, 0F, 1F, 1, 1, 1), ModelTransform.of(0F, 23F, 0F, 0F, -0.8551081F, 0F));
        return TexturedModelData.of(data, 64, 32);
    }

    static TexturedModelData stage2() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("one", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-2F, -3F, -3F, 4, 4, 4), ModelTransform.of(-1F, 24F, -1F, 0.1115358F, 0.1858931F, -0.1487144F));
        root.addChild("two", ModelPartBuilder.create().uv(0, 9).mirrored().cuboid(0F, -2.5F, -2F, 3, 3, 3), ModelTransform.of(0.5F, 24F, -1F, 0.0371786F, -0.0371786F, 0.1115358F));
        root.addChild("three", ModelPartBuilder.create().uv(0, 16).mirrored().cuboid(-1F, -2.5F, 0.5F, 3, 3, 3), ModelTransform.of(0F, 24F, 0F, -0.1487144F, 0.3717861F, 0F));
        root.addChild("four", ModelPartBuilder.create().uv(0, 23).mirrored().cuboid(-2.5F, -2F, 0F, 2, 2, 2), ModelTransform.of(0F, 24F, 0F, 0F, 0.0371786F, -0.1115358F));
        root.addChild("five", ModelPartBuilder.create().uv(0, 28).mirrored().cuboid(-1F, -1.5F, 0F, 2, 2, 2), ModelTransform.of(3F, 24F, 0F, -0.2602503F, 0.6320364F, 0F));
        root.addChild("six", ModelPartBuilder.create().uv(17, 0).mirrored().cuboid(0F, -1F, 1F, 1, 1, 1), ModelTransform.of(0F, 24F, 2F, 0F, -0.8922867F, 0.2974289F));
        root.addChild("seven", ModelPartBuilder.create().uv(17, 3).mirrored().cuboid(-1F, -1F, -1F, 1, 1, 1), ModelTransform.of(-3F, 24F, -1F, 0.0743572F, 0.1487144F, 0F));
        return TexturedModelData.of(data, 64, 32);
    }

    static TexturedModelData stage3() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("one", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(-2F, -3F, -3F, 4, 4, 4), ModelTransform.of(-1F, 24F, -1F, 0.1115358F, 0.1858931F, -0.1487144F));
        root.addChild("two", ModelPartBuilder.create().uv(0, 9).mirrored().cuboid(0F, -2.5F, -3F, 4, 3, 4), ModelTransform.of(0.5F, 24F, -1F, 0.1858931F, -0.2602503F, 0.1115358F));
        root.addChild("three", ModelPartBuilder.create().uv(0, 17).mirrored().cuboid(-2F, -2.5F, 0.5F, 4, 3, 4), ModelTransform.of(0F, 24F, 0F, -0.1487144F, 0.3717861F, 0F));
        root.addChild("four", ModelPartBuilder.create().uv(0, 25).mirrored().cuboid(-3.5F, -2F, 0F, 3, 3, 3), ModelTransform.of(0F, 24F, 0F, -0.1487144F, -0.1335332F, -0.1115358F));
        root.addChild("five", ModelPartBuilder.create().uv(17, 10).mirrored().cuboid(-1F, -2F, 0F, 3, 3, 3), ModelTransform.of(3F, 24F, 0F, -0.2602503F, 0.6320364F, 0F));
        root.addChild("six", ModelPartBuilder.create().uv(17, 0).mirrored().cuboid(0F, -1.5F, 1F, 2, 2, 2), ModelTransform.of(0F, 24F, 2F, 0F, -1.375609F, 0.2974289F));
        root.addChild("seven", ModelPartBuilder.create().uv(17, 5).mirrored().cuboid(-2F, -1F, -1F, 2, 2, 2), ModelTransform.of(-3F, 24F, -1F, 0.0743572F, 0.2230717F, -0.2230717F));
        root.addChild("eight", ModelPartBuilder.create().uv(17, 16).mirrored().cuboid(0F, -1.5F, -1F, 2, 2, 2), ModelTransform.of(0F, 24F, -5F, 0.1487144F, -0.2602503F, 0F));
        root.addChild("nine", ModelPartBuilder.create().uv(17, 21).mirrored().cuboid(-1F, -1F, 0F, 1, 1, 1), ModelTransform.of(-2F, 24F, -5F, 0F, -0.2602503F, -0.2974289F));
        root.addChild("ten", ModelPartBuilder.create().uv(17, 24).mirrored().cuboid(0F, -1F, 0F, 1, 1, 1), ModelTransform.of(0F, 24F, 5F, -0.1487144F, 0.1487144F, 0F));
        root.addChild("flower1", ModelPartBuilder.create().uv(26, 0).mirrored().cuboid(0F, -5F, -2.5F, 0, 2, 3), ModelTransform.of(-1F, 24F, -1F, 0.111544F, 0.185895F, -0.1487195F));
        root.addChild("flower2", ModelPartBuilder.create().uv(26, 0).mirrored().cuboid(-1.5F, -5F, -1F, 3, 2, 0), ModelTransform.of(-1F, 24F, -1F, 0.111544F, 0.185895F, -0.1487195F));
        return TexturedModelData.of(data, 64, 32);
    }
}
