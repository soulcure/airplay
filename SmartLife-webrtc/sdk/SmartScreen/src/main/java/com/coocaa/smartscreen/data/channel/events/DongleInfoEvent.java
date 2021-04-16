package com.coocaa.smartscreen.data.channel.events;

public class DongleInfoEvent {

    private boolean isSystemUpgradeExist;

    public void setIsSystemUpgradeExist(boolean isSystemUpgradeExist) {
        this.isSystemUpgradeExist = isSystemUpgradeExist;
    }

    public boolean getIsSystemUpgradeExist() {
        return isSystemUpgradeExist;
    }
}
