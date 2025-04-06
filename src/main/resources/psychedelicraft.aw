accessWidener       v1       named
accessible          class    net/minecraft/client/render/RenderLayer$MultiPhaseParameters
accessible          class    net/minecraft/client/render/RenderPhase$TextureBase

accessible          class    net/minecraft/client/particle/BlockLeakParticle$Dripping
accessible          class    net/minecraft/client/particle/BlockLeakParticle$ContinuousFalling

accessible          method   net/minecraft/client/particle/BlockLeakParticle$Dripping          <init>      (Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/fluid/Fluid;Lnet/minecraft/particle/ParticleEffect;)V
accessible          method   net/minecraft/client/particle/BlockLeakParticle$ContinuousFalling <init>      (Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/fluid/Fluid;Lnet/minecraft/particle/ParticleEffect;)V

accessible          method   net/minecraft/world/poi/PointOfInterestTypes          register   (Lnet/minecraft/registry/Registry;Lnet/minecraft/registry/RegistryKey;Ljava/util/Set;II)Lnet/minecraft/world/poi/PointOfInterestType;
accessible          method   net/minecraft/client/render/RenderLayer$MultiPhase    getPhases  ()Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters;
accessible          method   net/minecraft/client/render/RenderPhase$TextureBase   getId      ()Ljava/util/Optional;

accessible          field    net/minecraft/client/render/RenderLayer$MultiPhaseParameters     texture        Lnet/minecraft/client/render/RenderPhase$TextureBase;
accessible          field    net/minecraft/item/ItemGroups                                    displayContext Lnet/minecraft/item/ItemGroup$DisplayContext;

mutable             field    net/minecraft/loot/LootTable                                     pools          Ljava/util/List;
accessible          field    net/minecraft/loot/LootTable                                     pools          Ljava/util/List;

extendable          method   net/minecraft/block/CropBlock                                    isMature    (Lnet/minecraft/block/BlockState;)Z