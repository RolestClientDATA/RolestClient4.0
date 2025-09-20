package minecraft.rolest.ui.autobuy.api.donate;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DonateItems {

    public static ArrayList<ItemStack> donitem = new ArrayList<>();

    public static void add() {



        {


            ItemStack desorientationItem = new ItemStack(Items.ENDER_EYE);
            desorientationItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Дезориентация"));

            ItemStack crusherSwordItem = new ItemStack(Items.NETHERITE_SWORD);
            crusherSwordItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Меч Крушителя"));

            ItemStack katanaItem = new ItemStack(Items.NETHERITE_SWORD);
            katanaItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Катана"));

            ItemStack satansSwordItem = new ItemStack(Items.NETHERITE_SWORD);
            satansSwordItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Меч Сатаны"));

            ItemStack plastItem = new ItemStack(Items.DRIED_KELP);
            plastItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Пласт"));

            ItemStack obviousDustItem = new ItemStack(Items.SUGAR);
            obviousDustItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Явная пыль"));

            ItemStack tridentCrusherItem = new ItemStack(Items.TRIDENT);
            tridentCrusherItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Трезубец Крушителя"));

            ItemStack crusherBowItem = new ItemStack(Items.BOW);
            crusherBowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Лук Крушителя"));

            ItemStack satansBowItem = new ItemStack(Items.BOW);
            satansBowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Лук Сатаны"));

            ItemStack phantomBowItem = new ItemStack(Items.BOW);
            phantomBowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Лук Фантома"));

            ItemStack crossbowCrusherItem = new ItemStack(Items.CROSSBOW);
            crossbowCrusherItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Арбалет Крушителя"));

            ItemStack trapItem = new ItemStack(Items.NETHERITE_SCRAP);
            trapItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Трапка"));

            ItemStack newYearPlastItem = new ItemStack(Items.DRIED_KELP);
            newYearPlastItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Новогодний Пласт"));

            ItemStack freezingSnowballItem = new ItemStack(Items.SNOWBALL);
            freezingSnowballItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Снежок заморозка"));

            ItemStack newYearTrapItem = new ItemStack(Items.NETHERITE_SCRAP);
            newYearTrapItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Новогодняя Трапка"));

            ItemStack satansHelmetItem = new ItemStack(Items.NETHERITE_HELMET);
            satansHelmetItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Шлем Сатаны"));

            ItemStack crusherHelmetItem = new ItemStack(Items.NETHERITE_HELMET);
            crusherHelmetItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Шлем Крушителя"));

            ItemStack satansChestplateItem = new ItemStack(Items.NETHERITE_CHESTPLATE);
            satansChestplateItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Нагрудник Сатаны"));

            ItemStack crusherChestplateItem = new ItemStack(Items.NETHERITE_CHESTPLATE);
            crusherChestplateItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Нагрудник Крушителя"));

            ItemStack satansLeggingsItem = new ItemStack(Items.NETHERITE_LEGGINGS);
            satansLeggingsItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Поножи Сатаны"));

            ItemStack crusherLeggingsItem = new ItemStack(Items.NETHERITE_LEGGINGS);
            crusherLeggingsItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Поножи Крушителя"));


            ItemStack satansBootsItem = new ItemStack(Items.NETHERITE_BOOTS);
            satansBootsItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Ботинки Сатаны"));

            ItemStack crusherBootsItem = new ItemStack(Items.NETHERITE_BOOTS);
            crusherBootsItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Ботинки Крушителя"));

            ItemStack devilArrowItem = new ItemStack(Items.ARROW);
            devilArrowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Дьявольская стрела"));

            ItemStack sharpArrowItem = new ItemStack(Items.ARROW);
            sharpArrowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Точеная стрела"));

            ItemStack paranoiaArrowItem = new ItemStack(Items.ARROW);
            paranoiaArrowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Стрела паранойи"));

            ItemStack jesterArrowItem = new ItemStack(Items.ARROW);
            jesterArrowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Стрела Джастера"));

            ItemStack icyArrowItem = new ItemStack(Items.ARROW);
            icyArrowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Ледяная стрела"));

            ItemStack poisonousArrowItem = new ItemStack(Items.ARROW);
            poisonousArrowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Ядовитая стрела"));

            ItemStack cursedArrowItem = new ItemStack(Items.ARROW);
            cursedArrowItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Проклятая стрела"));

            ItemStack potionOfStrengthItem = new ItemStack(Items.POTION);
            potionOfStrengthItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье силы"));

            ItemStack potionOfInvisibilityItem = new ItemStack(Items.POTION);
            potionOfInvisibilityItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье невидимости"));

            ItemStack potionOfSwiftnessItem = new ItemStack(Items.POTION);
            potionOfSwiftnessItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье скорости"));

            ItemStack potionOfLeapingItem = new ItemStack(Items.POTION);
            potionOfLeapingItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье прыгучести"));

            ItemStack potionOfRegenerationItem = new ItemStack(Items.POTION);
            potionOfRegenerationItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье регенерации"));

            ItemStack nightVisionPotionItem = new ItemStack(Items.POTION);
            nightVisionPotionItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье ночного зрения"));

            ItemStack fireResistancePotionItem = new ItemStack(Items.POTION);
            fireResistancePotionItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье огнестойкости"));

            ItemStack waterBreathingPotionItem = new ItemStack(Items.POTION);
            waterBreathingPotionItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье водного дыхания"));

            ItemStack flashPotionItem = new ItemStack(Items.SPLASH_POTION);
            flashPotionItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Моча флеша"));

            ItemStack medicPotionItem = new ItemStack(Items.SPLASH_POTION);
            medicPotionItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье Медика"));

            ItemStack agentPotionItem = new ItemStack(Items.SPLASH_POTION);
            agentPotionItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье Агента"));

            ItemStack winnerPotionItem = new ItemStack(Items.SPLASH_POTION);
            winnerPotionItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье Победителя"));

            ItemStack killerPotionItem = new ItemStack(Items.SPLASH_POTION);
            killerPotionItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье Киллера"));

            ItemStack burpPotionItem = new ItemStack(Items.SPLASH_POTION);
            burpPotionItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Зелье Отрыжки"));

            ItemStack sulfuricAcidItem = new ItemStack(Items.SPLASH_POTION);
            sulfuricAcidItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Серная кислота"));

            ItemStack flashItem = new ItemStack(Items.SPLASH_POTION);
            flashItem.setDisplayName(ITextComponent.getTextComponentOrEmpty("Вспышка"));
            donitem.addAll(List.of(
                    desorientationItem,
                    crusherSwordItem,
                    katanaItem,
                    satansSwordItem,
                    plastItem,
                    obviousDustItem,
                    tridentCrusherItem,
                    crusherBowItem,
                    satansBowItem,
                    phantomBowItem,
                    crossbowCrusherItem,
                    trapItem,
                    newYearPlastItem,
                    freezingSnowballItem,
                    newYearTrapItem,
                    satansHelmetItem,
                    crusherHelmetItem,
                    satansChestplateItem,
                    crusherChestplateItem,
                    satansLeggingsItem,
                    crusherLeggingsItem,
                    satansBootsItem,
                    crusherBootsItem,
                    devilArrowItem,
                    sharpArrowItem,
                    paranoiaArrowItem,
                    jesterArrowItem,
                    icyArrowItem,
                    poisonousArrowItem,
                    cursedArrowItem,
                    potionOfStrengthItem,
                    potionOfInvisibilityItem,
                    potionOfSwiftnessItem,
                    potionOfLeapingItem,
                    potionOfRegenerationItem,
                    nightVisionPotionItem,
                    fireResistancePotionItem,
                    waterBreathingPotionItem,
                    flashPotionItem,
                    medicPotionItem,
                    agentPotionItem,
                    winnerPotionItem,
                    killerPotionItem,
                    burpPotionItem,
                    sulfuricAcidItem,
                    flashItem
            ));


        }
        HashMap<Enchantment, Integer> fake = new HashMap<>();
        fake.put(Enchantments.UNBREAKING, 0);
        for (ItemStack s : donitem) {
            EnchantmentHelper.setEnchantments(fake, s);
        }
        //afina.setTag();
    }

    public static ItemStack add(String texture, String name) {
        try {
            ItemStack magma = new ItemStack(Items.PLAYER_HEAD);
            magma.setTag(JsonToNBT.getTagFromJson(String.format("{SkullOwner:{Id:[I;-1949909288,1299464445,-1707774066,-249984712],Properties:{textures:[{Value:\"%s\"}]},Name:\"%s\"}}", texture, name)));
            magma.setDisplayName(new StringTextComponent(name));
            return magma;
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
