package minecraft.rolest.ui.autobuy.api.factory;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;

import java.util.Map;

import minecraft.rolest.ui.autobuy.api.model.IItem;

public interface ItemFactory {
    IItem createNewItem(Item item, int price, int quantity, int damage, Map<Enchantment, Integer> enchantments);
}
