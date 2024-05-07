package fi.ugim.conflagration.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Ticks;

public abstract class CustomBomb implements Bomb {

    protected int customModelData;
    protected double damage;
    protected double radius;
    protected Ticks fuseTime;
    protected String name;
    protected Component displayName;
    protected BlockState blockState;

    @Override
    public int customModelData() {
        return this.customModelData;
    }

    @Override
    public double damage() {
        return this.damage;
    }

    @Override
    public double radius() {
        return this.radius;
    }

    @Override
    public Ticks fuseTime() {
        return this.fuseTime;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Component displayName() {
        return displayName;
    }

    @Override
    public BlockState blockState() {
        return this.blockState;
    }

    @Override
    public ItemStack itemStack() {
        return ItemStack.builder()
                .itemType(ItemTypes.HEART_OF_THE_SEA)
                .add(Keys.CUSTOM_MODEL_DATA, this.customModelData)
                .add(Keys.HIDE_ATTRIBUTES, true)
                .add(Keys.HIDE_MISCELLANEOUS, true)
                .add(Keys.HIDE_ENCHANTMENTS, true)
                .add(Keys.CUSTOM_NAME, this.displayName.append(Component.text("")).decoration(TextDecoration.ITALIC, false))
                .build();
    }

    protected void initialize() {
        StringBuilder text = new StringBuilder();
        for (Component child : this.displayName.children()) {
            text.append(((TextComponent) child).content());
        }
        this.name = text.toString().split("\\s+")[0].toLowerCase();
    }

}
