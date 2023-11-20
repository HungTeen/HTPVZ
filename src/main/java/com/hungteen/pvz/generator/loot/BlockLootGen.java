package com.hungteen.pvz.generator.loot;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.ArrayList;
import java.util.List;

public class BlockLootGen extends BlockLoot {

    public List<Block> lootedList = new ArrayList<>();
    private static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item()
            .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
    private static final LootItemCondition.Builder HAS_SHEARS = MatchTool
            .toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);

    @Override
    public void addTables(){
        //init
        PVZBlocks.lootedList.forEach((obj) -> {
            lootedList.add(obj.get());
        });
        //leaves
        PVZBlocks.woodList.forEach((map) -> {
                this.addLeavesDrops(map.get(PVZBlocks.WoodSet.Leaves).get(), map.get(PVZBlocks.WoodSet.Sampling).get(), NORMAL_LEAVES_SAPLING_CHANCES);
                this.addDoorTable(map.get(PVZBlocks.WoodSet.Door).get());
        }
        );

        this.dropOther(PVZBlocks.NUT_LEAVES_WITH_NUTS.get(), PVZItems.NUT.get());
        this.add(PVZBlocks.CARP_GRASS.get(), noDrop());
        this.addOreDrop(PVZBlocks.ORIGIN_ORE.get(), PVZItems.ORIGIN_ESSENCE.get());
        //TODO bug that essence alter and origin block drop self without tool.

        //the rest
        PVZBlocks.BLOCKS.getEntries().forEach((blockObj) ->{
            if (blockObj.getId().getNamespace().equals(PVZMod.MODID) && !lootedList.contains(blockObj.get()) && blockObj.get().asItem() != Items.AIR) {
                outPut(blockObj.get());
                this.dropSelf(blockObj.get());
                lootedList.add(blockObj.get());
            }
        });
    }
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return new ArrayList<>(lootedList);
    }

    protected void addLeavesDrops(Block block, Block sapling, float... chance) {
        outPut(block);
        lootedList.add(block);
        this.add(block, createLeavesDrops(block, sapling, chance));
    }
    protected void addDoorTable(Block block) {
        outPut(block);
        lootedList.add(block);
        this.add(block, createDoorTable(block));
    }
    protected void addOreDrop(Block block, Item item) {
        outPut(block);
        lootedList.add(block);
        this.add(block, createOreDrop(block, item));
    }

    private void outPut(Block block){
        PVZMod.LOGGER.info("Gen Block Loot Table: " + block.getDescriptionId());
    }
}
