package minecraft.rolest.modules.impl.misc;


import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import minecraft.rolest.events.EventUpdate;
import minecraft.rolest.modules.api.Category;
import minecraft.rolest.modules.api.Module;
import minecraft.rolest.modules.api.ModuleRegister;
import minecraft.rolest.modules.settings.Setting;
import minecraft.rolest.modules.settings.impl.BooleanSetting;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.Iterator;

@Getter
@Setter
/* Ес чо вместо Util используйте Misc или что вам надо */
@ModuleRegister(name = "AhHelper", category = Category.Misc,desc ="Помощник аукциона")
public class AhHelper extends Module {
    public BooleanSetting three = new BooleanSetting("Подсвечивать 3 слота", true);
    float x = 0.0F;
    float y = 0.0F;
    float x2 = 0.0F;
    float y2 = 0.0F;
    float x3 = 0.0F;
    float y3 = 0.0F;

    public AhHelper() {
        this.addSettings(new Setting[]{this.three});
    }

    @Subscribe
    public void onUpdate(EventUpdate update) {
        Screen var3 = mc.currentScreen;
        if (var3 instanceof ChestScreen e) {
            if (!e.getTitle().getString().contains("Аукцион") && !e.getTitle().getString().contains("Поиск:")) {
                this.setX(0.0F);
                this.setX2(0.0F);
                this.setX3(0.0F);
            } else {
                Container container = e.getContainer();
                Slot slot1 = null;
                Slot slot2 = null;
                Slot slot3 = null;
                int fsPrice = Integer.MAX_VALUE;
                int medPrice = Integer.MAX_VALUE;
                int thPrice = Integer.MAX_VALUE;
                boolean b = false;
                Iterator var11 = container.inventorySlots.iterator();

                while(var11.hasNext()) {
                    Slot slot = (Slot)var11.next();
                    if (slot.slotNumber <= 44) {
                        int currentPrice = this.extractPriceFromStack(slot.getStack());
                        if (currentPrice != -1 && currentPrice < fsPrice) {
                            fsPrice = currentPrice;
                            slot1 = slot;
                        }

                        if ((Boolean)this.three.get()) {
                            if (currentPrice != -1 && currentPrice < medPrice && currentPrice > fsPrice) {
                                medPrice = currentPrice;
                                slot2 = slot;
                            }

                            if (currentPrice != -1 && currentPrice < thPrice && currentPrice > medPrice) {
                                thPrice = currentPrice;
                                slot3 = slot;
                            }
                        } else {
                            this.setX2(0.0F);
                            this.setX3(0.0F);
                        }
                    }
                }

                if (slot1 != null) {
                    this.setX((float)slot1.xPos);
                    this.setY((float)slot1.yPos);
                }

                if (slot2 != null) {
                    this.setX2((float)slot2.xPos);
                    this.setY2((float)slot2.yPos);
                }

                if (slot3 != null) {
                    this.setX3((float)slot3.xPos);
                    this.setY3((float)slot3.yPos);
                }
            }
        } else {
            this.setX(0.0F);
            this.setX2(0.0F);
            this.setX3(0.0F);
        }

    }

    protected int extractPriceFromStack(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("display", 10)) {
            CompoundNBT display = tag.getCompound("display");
            if (display.contains("Lore", 9)) {
                ListNBT lore = display.getList("Lore", 8);

                for(int j = 0; j < lore.size(); ++j) {
                    JsonObject object = (new JsonParser()).parse(lore.getString(j)).getAsJsonObject();
                    if (object.has("extra")) {
                        JsonArray array = object.getAsJsonArray("extra");
                        if (array.size() > 2) {
                            JsonObject title = array.get(1).getAsJsonObject();
                            if (title.get("text").getAsString().trim().toLowerCase().contains("ценa")) {
                                String line = array.get(2).getAsJsonObject().get("text").getAsString().trim().substring(1).replaceAll(",", "");
                                return Integer.parseInt(line);
                            }
                        }
                    }
                }
            }
        }

        return -1;
    }

    public BooleanSetting getThree() {
        return this.three;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getX2() {
        return this.x2;
    }

    public float getY2() {
        return this.y2;
    }

    public float getX3() {
        return this.x3;
    }

    public float getY3() {
        return this.y3;
    }

    public void setThree(BooleanSetting three) {
        this.three = three;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public void setX3(float x3) {
        this.x3 = x3;
    }

    public void setY3(float y3) {
        this.y3 = y3;
    }
}
