package com.io.norabotics.client.screen.base;

public interface IBaseGui {

    void addSubGui(IElement subGui);

    void removeSubGui();

    boolean hasSubGui();

    IElement getSubGui();

}
