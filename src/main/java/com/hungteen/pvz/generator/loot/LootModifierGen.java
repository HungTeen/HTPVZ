package com.hungteen.pvz.generator.loot;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.event.RegisterSproutsEvent;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.item.SproutItem;
import com.hungteen.pvz.common.loot.AddItemModifier;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import java.util.List;

public class LootModifierGen extends GlobalLootModifierProvider {
    public LootModifierGen(DataGenerator gen) {
        super(gen, PVZMod.MODID);
    }

    @Override
    protected void start() {
        //drops
        this.add("cabbage_seed_from_grass", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GRASS).build(),
                LootItemRandomChanceCondition.randomChance(0.025F).build()
        }, List.of(PVZItems.CABBAGE_SEED.get().getDefaultInstance())));

        this.add("pea_from_grass", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GRASS).build(),
                LootItemRandomChanceCondition.randomChance(0.125F).build()
        }, List.of(PVZItems.PEA.get().getDefaultInstance())));

        this.add("corn_kernels_from_grass", new AddItemModifier(new LootItemCondition[] {
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GRASS).build(),
                LootItemRandomChanceCondition.randomChance(0.025F).build()
        }, List.of(PVZItems.CORN_KERNELS.get().getDefaultInstance())));

        //spawn bonus chest
        this.add("seed_packets_from_spawn_bonus_chest", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(new ResourceLocation("chests/spawn_bonus_chest")).build()
        }, List.of(
                SeedPacketItem.getSeedPacket(PVZEntities.PEA_SHOOTER.get()).getDefaultInstance(),
                SeedPacketItem.getSeedPacket(PVZEntities.SUN_FLOWER.get()).getDefaultInstance(),
                SeedPacketItem.getSeedPacket(PVZEntities.WALL_NUT.get()).getDefaultInstance()
        )));

        //sprouts
        this.add("sprout_from_buried_treasure", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(new ResourceLocation("chests/buried_treasure")).build(),
                LootItemRandomChanceCondition.randomChance(0.75F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.water", RegisterSproutsEvent.WATER),
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.water", RegisterSproutsEvent.WATER)
        )));

        this.add("sprout_from_shipwreck_supply", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(new ResourceLocation("chests/shipwreck_supply")).build(),
                LootItemRandomChanceCondition.randomChance(0.45F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.water", RegisterSproutsEvent.WATER)
        )));

        this.add("sprout_from_igloo", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(new ResourceLocation("chests/igloo_chest")).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.icy", RegisterSproutsEvent.ICY)
        )));

        this.add("sprout_from_igloo_random", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(new ResourceLocation("chests/igloo_chest")).build(),
                LootItemRandomChanceCondition.randomChance(0.6F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.icy", RegisterSproutsEvent.ICY)
        )));

        this.add("sprout_from_village_snowy_house", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(new ResourceLocation("chests/village/village_snowy_house")).build(),
                LootItemRandomChanceCondition.randomChance(0.8F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.icy", RegisterSproutsEvent.ICY)
        )));

        this.add("sprout_from_village_taiga_house", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(new ResourceLocation("chests/village/village_taiga_house")).build(),
                LootItemRandomChanceCondition.randomChance(0.5F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.common", RegisterSproutsEvent.COMMON)
        )));

        this.add("sprout_from_village_shepherd", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(new ResourceLocation("chests/village/village_shepherd")).build(),
                LootItemRandomChanceCondition.randomChance(0.5F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.common", RegisterSproutsEvent.COMMON)
        )));

        this.add("sprout_from_abandoned_mineshaft", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(new ResourceLocation("chests/abandoned_mineshaft")).build(),
                LootItemRandomChanceCondition.randomChance(0.5F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.common", RegisterSproutsEvent.COMMON)
        )));

        this.add("sprout_from_nether_bridge", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(new ResourceLocation("chests/nether_bridge")).build(),
                LootItemRandomChanceCondition.randomChance(0.8F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.nether_aggressive", RegisterSproutsEvent.NETHER_AGGRESSIVE),
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.nether_aggressive", RegisterSproutsEvent.NETHER_AGGRESSIVE)
        )));

        this.add("sprout_from_bastion_treasure", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(new ResourceLocation("chests/bastion_treasure")).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.nether_defencive", RegisterSproutsEvent.NETHER_DEFENCIVE)
        )));

        this.add("sprout_from_bastion_bridge", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(new ResourceLocation("chests/bastion_bridge")).build(),
                LootItemRandomChanceCondition.randomChance(0.6F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.nether_defencive", RegisterSproutsEvent.NETHER_DEFENCIVE)
        )));

        this.add("sprout_from_ruined_portal", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(new ResourceLocation("chests/ruined_portal")).build(),
                LootItemRandomChanceCondition.randomChance(0.4F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.nether_aggressive", RegisterSproutsEvent.NETHER_AGGRESSIVE)
        )));

        this.add("sprout_from_city_end_treasure", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(new ResourceLocation("chests/city_end_treasure")).build(),
                LootItemRandomChanceCondition.randomChance(0.4F).build()
        }, List.of(
                SproutItem.getTaggedItem((SproutItem) PVZItems.SPROUT.get(), "sprout.pvz.ender", RegisterSproutsEvent.ENDER)
        )));
    }
}
