package ivorius.psychedelicraft.network;

import com.sollace.fabwork.api.packets.S2CPacketType;
import com.sollace.fabwork.api.packets.SimpleNetworking;

import ivorius.psychedelicraft.Psychedelicraft;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface Channel {
    S2CPacketType<MsgDrugProperties> UPDATE_DRUG_PROPERTIES = SimpleNetworking.serverToClient(Psychedelicraft.id("update_drug_properties"), MsgDrugProperties::new);
    S2CPacketType<MsgHallucinate> HALLUCINATE = SimpleNetworking.serverToClient(Psychedelicraft.id("hallucinate"), MsgHallucinate::new);

    static void bootstrap() { }

}
