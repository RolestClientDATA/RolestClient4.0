package minecraft.rolest.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import minecraft.rolest.events.TickEvent;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import net.minecraft.item.Items;

@ModuleRegister(name = "FastEXP", category = Category.Player)
public class FastEXP extends Module {
@Subscribe
public void onEvent(TickEvent e) {
    if (mc.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
        mc.rightClickDelayTimer = 1;
    }
}
}