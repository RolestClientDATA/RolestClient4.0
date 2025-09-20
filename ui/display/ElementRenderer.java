package minecraft.rolest.ui.display;

import minecraft.rolest.events.EventDisplay;
import minecraft.rolest.utils.client.IMinecraft;

public interface ElementRenderer extends IMinecraft {
    void render(EventDisplay eventDisplay);
}
