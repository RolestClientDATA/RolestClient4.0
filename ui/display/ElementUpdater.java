package minecraft.rolest.ui.display;

import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.utils.client.IMinecraft;

public interface ElementUpdater extends IMinecraft {

    void update(EventUpdate e);
}
