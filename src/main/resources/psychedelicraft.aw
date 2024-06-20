accessWidener       v1       named
accessible          class    net/minecraft/client/particle/BlockLeakParticle$Dripping
accessible          class    net/minecraft/client/particle/BlockLeakParticle$ContinuousFalling

accessible          method   net/minecraft/client/particle/BlockLeakParticle$Dripping          <init>      (Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/fluid/Fluid;Lnet/minecraft/particle/ParticleEffect;)V
accessible          method   net/minecraft/client/particle/BlockLeakParticle$ContinuousFalling <init>      (Lnet/minecraft/client/world/ClientWorld;DDDLnet/minecraft/fluid/Fluid;Lnet/minecraft/particle/ParticleEffect;)V

accessible          method   net/minecraft/world/poi/PointOfInterestTypes          register   (Lnet/minecraft/registry/Registry;Lnet/minecraft/registry/RegistryKey;Ljava/util/Set;II)Lnet/minecraft/world/poi/PointOfInterestType;

accessible          method   net/minecraft/world/GameRules               register             (Ljava/lang/String;Lnet/minecraft/world/GameRules$Category;Lnet/minecraft/world/GameRules$Type;)Lnet/minecraft/world/GameRules$Key;
accessible          method   net/minecraft/world/GameRules$BooleanRule   create               (Z)Lnet/minecraft/world/GameRules$Type;
accessible          method   net/minecraft/world/GameRules$IntRule       create               (I)Lnet/minecraft/world/GameRules$Type;

accessible          field    net/minecraft/item/ItemGroups                                    displayContext Lnet/minecraft/item/ItemGroup$DisplayContext;

mutable             field    net/minecraft/loot/LootTable                                     pools          Ljava/util/List;
accessible          field    net/minecraft/loot/LootTable                                     pools          Ljava/util/List;

extendable          method   net/minecraft/block/CropBlock                                    isMature    (Lnet/minecraft/block/BlockState;)Z