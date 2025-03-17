package ivorius.psychedelicraft.config;

public enum MessageDistortion {
    INCOMING,
    OUTGOING,
    BOTH,
    NONE;

    public boolean incoming() {
        return this == INCOMING || this == BOTH;
    }

    public boolean outgoing() {
        return this == OUTGOING || this == BOTH;
    }
}