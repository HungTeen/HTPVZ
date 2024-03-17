package com.hungteen.pvz.generator.loot;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
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
                this.addLeavesDrops(map.get(PVZBlocks.WoodSet.Leaves).get(), map.get(PVZBlocks.WoodSet.Sapling).get(), NORMAL_LEAVES_SAPLING_CHANCES);
                this.addPottedDrop(map.get(PVZBlocks.WoodSet.PottedSapling).get());
                this.addDoorTable(map.get(PVZBlocks.WoodSet.Door).get());
                this.addSlabDrop(map.get(PVZBlocks.WoodSet.Slab).get());
        }
        );
        this.addCropDrop(PVZBlocks.PEA.get(), PVZItems.PEA.get(), PVZItems.PEA.get(), LootItemBlockStatePropertyCondition.hasBlockStateProperties(PVZBlocks.PEA.get()).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7)));
        this.addCropDrop(PVZBlocks.CABBAGE_SEEDS.get(), PVZItems.CABBAGE.get(), PVZItems.CABBAGE_SEED.get(), LootItemBlockStatePropertyCondition.hasBlockStateProperties(PVZBlocks.CABBAGE_SEEDS.get()).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7)));
        this.addCropDrop(PVZBlocks.CORN_KERNELS.get(), PVZItems.CORN.get(), PVZItems.CORN_KERNELS.get(), LootItemBlockStatePropertyCondition.hasBlockStateProperties(PVZBlocks.CORN_KERNELS.get()).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7)));
        this.dropOther(PVZBlocks.NUT_LEAVES_WITH_NUTS.get(), PVZItems.NUT.get());
        this.addOreDrop(PVZBlocks.ORIGIN_ORE.get(), PVZItems.ORIGIN_ESSENCE.get());
        this.addSlabDrop(PVZBlocks.GARDEN_FLOWER_POT.get());
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
    protected void addSlabDrop(Block block) {
        outPut(block);
        lootedList.add(block);
        this.add(block, createSlabItemTable(block));
    }
    protected void addPottedDrop(Block block) {
        outPut(block);
        lootedList.add(block);
        this.add(block, createPotFlowerItemTable(((FlowerPotBlock)block).getContent()));
    }
    protected void addCropDrop(Block block, Item result, Item seed, LootItemCondition.Builder condition) {
        outPut(block);
        lootedList.add(block);
        this.add(block, createCropDrops(block, result, seed, condition));
    }

    private void outPut(Block block){
        PVZMod.LOGGER.info("Gen Block Loot Table: " + block.getDescriptionId());
    }
}
