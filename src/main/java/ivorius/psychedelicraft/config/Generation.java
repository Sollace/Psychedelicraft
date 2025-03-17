package ivorius.psychedelicraft.config;

public record Generation (
FeatureCustomConfig juniper,
FeatureCustomConfig cannabis,
FeatureCustomConfig hop,
FeatureCustomConfig tobacco,
FeatureCustomConfig morningGlories,
FeatureCustomConfig belladonna,
FeatureCustomConfig jimsonweed,
FeatureCustomConfig tomato,
FeatureCustomConfig coffea,
FeatureCustomConfig coca,
FeatureCustomConfig peyote,

boolean farmerDrugDeals,
boolean dungeonChests,
boolean villageChests) {}