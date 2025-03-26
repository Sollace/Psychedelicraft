package ivorius.psychedelicraft.entity.drug;

public interface DrugPropertiesContainer {
    DrugProperties getDrugProperties();

    interface Mutable extends DrugPropertiesContainer {
        void setDrugProperties(DrugProperties properties);
    }
}
