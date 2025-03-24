accessWidener       v2       named
transitive-accessible          class    net/minecraft/client/particle/BlockLeakParticle$Dripping
transitive-accessible          class    net/minecraft/client/particle/BlockLeakParticle$ContinuousFalling

transitive-accessible          method   net/minecraft/client/particle/BlockLeakParticle$Dripping          <init>      (Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/fluid/Fluid;Lnet/minecraft/particle/ParticleEffect;)V
transitive-accessible          method   net/minecraft/client/particle/BlockLeakParticle$ContinuousFalling <init>      (Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/fluid/Fluid;Lnet/minecraft/particle/ParticleEffect;)V

transitive-accessible          method   net/minecraft/world/poi/PointOfInterestTypes          register   (Lnet/minecraft/registry/Registry;Lnet/minecraft/registry/RegistryKey;Ljava/util/Set;II)Lnet/minecraft/world/poi/PointOfInterestType;

transitive-accessible          method   net/minecraft/world/GameRules               register             (Ljava/lang/String;Lnet/minecraft/world/GameRules$Category;Lnet/minecraft/world/GameRules$Type;)Lnet/minecraft/world/GameRules$Key;
transitive-accessible          method   net/minecraft/world/GameRules$BooleanRule   create               (Z)Lnet/minecraft/world/GameRules$Type;
transitive-accessible          method   net/minecraft/world/GameRules$IntRule       create               (I)Lnet/minecraft/world/GameRules$Type;

transitive-accessible          field    net/minecraft/item/ItemGroups                                    displayContext Lnet/minecraft/item/ItemGroup$DisplayContext;

transitive-mutable             field    net/minecraft/loot/LootTable                                     pools          Ljava/util/List;
transitive-accessible          field    net/minecraft/loot/LootTable                                     pools          Ljava/util/List;

transitive-accessible          class    net/minecraft/datafixer/fix/ItemStackComponentizationFix$StackData

transitive-extendable          method   net/minecraft/block/CropBlock                                    isMature    (Lnet/minecraft/block/BlockState;)Z


transitive-accessible class    net/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder

transitive-accessible method   net/minecraft/client/gl/RenderPipelines                                    register             (Lcom/mojang/blaze3d/pipeline/RenderPipeline;)Lcom/mojang/blaze3d/pipeline/RenderPipeline;

transitive-accessible method   net/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder       texturing            (Lnet/minecraft/client/render/RenderPhase$Texturing;)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;
transitive-accessible method   net/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder       texture              (Lnet/minecraft/client/render/RenderPhase$TextureBase;)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;
transitive-accessible method   net/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder       lightmap             (Lnet/minecraft/client/render/RenderPhase$Lightmap;)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;
transitive-accessible method   net/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder       layering             (Lnet/minecraft/client/render/RenderPhase$Layering;)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;
transitive-accessible method   net/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder       target               (Lnet/minecraft/client/render/RenderPhase$Target;)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder;
transitive-accessible method   net/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder       build                (Z)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters;

transitive-accessible field    net/minecraft/client/gl/RenderPipelines                           MATRICES_SNIPPET              Lcom/mojang/blaze3d/pipeline/RenderPipeline$Snippet;
transitive-accessible field    net/minecraft/client/gl/RenderPipelines                           FOG_SNIPPET                   Lcom/mojang/blaze3d/pipeline/RenderPipeline$Snippet;