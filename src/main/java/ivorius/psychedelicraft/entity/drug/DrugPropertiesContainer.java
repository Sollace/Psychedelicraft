package ivorius.psychedelicraft.entity.drug;

public interface DrugPropertiesContainer {
    DrugProperties getDrugProperties();

    interface Mutable {
        void setDrugProperties(DrugProperties properties);
    }
}
