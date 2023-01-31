// Made with Blockbench 4.6.1
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class custom_model extends EntityModel<Entity> {
	private final ModelPart bottle_0;
	public custom_model(ModelPart root) {
		this.bottle_0 = root.getChild("bottle_0");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bottle_0 = modelPartData.addChild("bottle_0", ModelPartBuilder.create().uv(0, 3).cuboid(-3.125F, -3.3F, 3.375F, 3.0F, 6.0F, 3.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-2.125F, -5.55F, 4.375F, 1.0F, 2.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(2.0F, 21.0F, -5.0F));
		return TexturedModelData.of(modelData, 32, 32);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		bottle_0.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}