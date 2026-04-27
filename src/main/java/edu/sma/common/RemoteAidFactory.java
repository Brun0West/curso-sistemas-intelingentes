package edu.sma.common;

import jade.core.AID;

public final class RemoteAidFactory {

    private RemoteAidFactory() {
    }

    public static AID build(String guid, String mtpAddress) {
        AID aid = new AID(guid, AID.ISGUID);
        if (mtpAddress != null && !mtpAddress.isBlank()) {
            aid.addAddresses(mtpAddress);
        }
        return aid;
    }
}
