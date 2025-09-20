package minecraft.rolest.ui.autobuy.api.factory;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;

import java.util.Map;

import minecraft.rolest.ui.autobuy.api.model.IItem;
import minecraft.rolest.ui.autobuy.api.model.ItemImpl;

public class ItemFactoryImpl implements ItemFactory {
    @Override
    public IItem createNewItem(Item item, int price, int quantity, int damage, Map<Enchantment, Integer> enchantments) {
        return new ItemImpl(item, price, quantity, damage, enchantments);
    }
}
