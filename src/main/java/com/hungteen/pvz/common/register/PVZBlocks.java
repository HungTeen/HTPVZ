package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.block.*;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.common.world.zen_garden.NutTreeGrower;
import com.hungteen.pvz.generator.loot.BlockLootGen;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import static com.hungteen.pvz.common.register.PVZItemTabs.PVZ_BLOCKS;

@SuppressWarnings("all")
public class PVZBlocks {

    //init
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PVZMod.MODID);
    private static CreativeModeTab storedTab = PVZ_BLOCKS;
    private static Supplier<Block> storedSup = () -> new Block(BlockBehaviour.Properties.of(Material.STONE));
    private static final PVZBlocks reflector = new PVZBlocks();
    public static List<WoodType> woodTypeList = new ArrayList<>();
    @Deprecated // will be cleared after register.
    public static List<Map<WoodSet, RegistryObject<Block>>> woodList = new ArrayList<>();
    //model
    private static Model storedModel = Model.Simple;
    private static List<ResourceLocation> storedModelTexture = List.of();
    private static Pair<PVZItems.Model, List<ResourceLocation>> storedItemModel = Pair.of(PVZItems.Model.Block, new ArrayList<>());
    private static boolean hasItem = true;
    private static boolean flowerPot = false;
    @Deprecated // will be cleared after register.
    public static List<Pair<RegistryObject<Block>, Pair<Model, List<ResourceLocation>>>> modelList = new ArrayList<>();
    //tag
    private static List<TagKey<Block>> storedTag = new ArrayList<>();
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject<Block>, List<TagKey<Block>>> tagMap = new HashMap<>();
    //renderType
    private static String storedRenderType = null;
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject<Block>, String> renderTypeMap = new HashMap<>();
    //blockEntity
    private static String storedBlockEntity = null;
    @Deprecated // will be cleared after register.
    public static Map<String, List<RegistryObject<Block>>> blockEntityMap = new HashMap<>();
    //flammable
    private static Pair<Integer, Integer> storedFlammable = new Pair<>(0, 0);
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject<Block>, Pair<Integer, Integer>> flammableMap = new HashMap<>();
    //loot
    private static Boolean storedLoot = true;
    @Deprecated // will be cleared after register.
    public static List<RegistryObject<Block>> lootedList = new ArrayList<>();
    //composter
    private static float storedComposterChance = 0;



    //registry
    //PVZ_BLOCKS
    public static final RegistryObject<Block> PLANTERN = composter(0.65F).tag(BlockTags.MINEABLE_WITH_AXE, BlockTags.REPLACEABLE_PLANTS).model(Model.Cross).itemModel(PVZItems.Model.Simple, res("plantern")).renderType("cutout").flammable(10, 5).block("plantern", () -> new FlowerBlock(PVZMobEffects.BRIGHTNESS, 5, BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).lightLevel(i -> 8)));
    public static final RegistryObject<Block> POTTED_PLANTERN = tag(BlockTags.FLOWER_POTS).flowerPot().model(Model.Potted, res("plantern")).renderType("cutout").noItem().block("potted_plantern", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, PLANTERN, BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion().lightLevel(i -> 8)));
    public static final RegistryObject<Block> NUT_LEAVES_WITH_NUTS = composter(0.65F).tag(BlockTags.LEAVES).renderType("cutout").flammable(30, 60).loot(false).block("nut_leaves_with_nuts", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final Map<WoodSet, RegistryObject<Block>> NUT = wood("nut", new NutTreeGrower());
    public static final RegistryObject<Block> CARP_GRASS = tag(/*PVZBlockTags.PLANTABLE_BLOCKS added in generator*/).model(Model.Modeled).renderType("cutout").flammable(5, 5).loot(false).block("carp_grass", () -> new CarpMossBlock(BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN).randomTicks().noLootTable()));
    public static final RegistryObject<Block> ORIGIN_BLOCK = tag(BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE).block("origin_block", () -> new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GREEN).strength(15, 50).lightLevel(i -> 15).sound(SoundType.ANCIENT_DEBRIS).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ORIGIN_ORE = tag(BlockTags.MINEABLE_WITH_SHOVEL).block("origin_ore", () -> new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_GREEN).strength(1F).sound(SoundType.GRASS).lightLevel(i -> 9)));
    public static final RegistryObject<Block> LUNAR_STONE = tag(BlockTags.MINEABLE_WITH_PICKAXE).block("lunar_stone", () -> new Block(Block.Properties.of(Material.STONE, MaterialColor.COLOR_YELLOW).strength(2F).sound(SoundType.STONE).lightLevel(i -> 8)));
    public static final RegistryObject<Block> WISDOM_TREE_CORE = tag(BlockTags.MINEABLE_WITH_AXE).loot(false).model(Model.Modeled).block("wisdom_tree_core", () -> new WisdomTreeCoreBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(-1.0F, 3600000.0F).noLootTable().randomTicks().lightLevel(i-> 12)));
    public static final RegistryObject<Block> WISDOM_TREE_LOG = tag(BlockTags.MINEABLE_WITH_AXE, BlockTags.LOGS).loot(false).model(Model.Column, res("wisdom_tree_log"), res("nut_log_top")).block("wisdom_tree_log", () -> new WisdomTreeLogBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(10F).noLootTable().randomTicks().lightLevel(i-> 3)));
    public static final RegistryObject<Block> PATTRA_LEAVES = composter(0.3F).tag(BlockTags.LEAVES).renderType("cutout").loot(false).block("pattra_leaves", () -> new PattraLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).strength(5F).noLootTable()));
    public static final RegistryObject<Block> ESSENCE_ALTAR = tag(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_PICKAXE).model(Model.Modeled).renderType("cutout").blockEntity("essence_altar").block("essence_altar", () -> new EssenceAltarBlock(BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)));
    public static final RegistryObject<Block> ESSENCE_FURNACE = tag(BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE).model(Model.Modeled).blockEntity("essence_furnace").block("essence_furnace", () -> new EssenceFurnaceBlock(BlockBehaviour.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(3.5F).lightLevel(litBlockEmission(13))));
    public static final RegistryObject<Block> GARDEN_FLOWER_POT = tag(BlockTags.MINEABLE_WITH_PICKAXE, PVZBlockTags.GARDEN_FLOWER_POT).model(Model.Modeled).itemModel(PVZItems.Model.Block).block("garden_flower_pot", () -> new GardenFlowerPotBlock(BlockBehaviour.Properties.of(Material.CLAY).strength(0.5F)));
    public static final RegistryObject<Block> WATERING_POT = model(Model.Modeled).noItem().block("watering_pot", () -> new WateringPotBlock(BlockBehaviour.Properties.of(Material.CLAY).strength(0F)));
    public static final RegistryObject<Block> ZEN_GARDEN_PORTAL = model(Model.Modeled).noItem(/*registered apart for making it stacks to 1.*/).block("zen_garden_portal", () -> new ZenGardenPortalBlock(BlockBehaviour.Properties.of(Material.CLAY).strength(0F).lightLevel(i-> 12)));

    //NO_TAB
    public static final RegistryObject<Block> ENTITY_LIGHT = loot(false).model(Model.Modeled).blockEntity("entity_light").noItem().block("entity_light", () -> new EntityLightBlock(BlockBehaviour.Properties.of(Material.AIR)
            .strength(-1.0F, 3600000.8F).noLootTable().noOcclusion().lightLevel(i -> i.getValue(EntityLightBlock.LEVEL))));

    //CROPS
    public static final RegistryObject<Block> PEA = tag(BlockTags.CROPS).model(Model.Modeled).renderType("cutout").noItem().block("pea", () -> new CropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)) {
        private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 10.0D, 14.0D), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D)};
        @Override public ItemLike getBaseSeedId() {return PVZItems.PEA.get();}
        public VoxelShape getShape(BlockState p_52297_, BlockGetter p_52298_, BlockPos p_52299_, CollisionContext p_52300_) {
            return SHAPE_BY_AGE[p_52297_.getValue(this.getAgeProperty())];
        }
    });
    public static final RegistryObject<Block> CABBAGE_SEEDS = tag(BlockTags.CROPS).model(Model.Modeled).renderType("cutout").noItem().block("cabbage_seeds", () -> new CropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)) {
        private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(6.0D, 0.0D, 6.0D, 10.0D, 2.0D, 10.0D), Block.box(6.0D, 0.0D, 6.0D, 10.0D, 2.0D, 10.0D), Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D), Block.box(4.0D, 0.0D, 4.0D, 12.0D, 4.0D, 12.0D), Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D), Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D), Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D), Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D)};
        @Override public ItemLike getBaseSeedId() {return PVZItems.CABBAGE_SEED.get();}
        public VoxelShape getShape(BlockState p_52297_, BlockGetter p_52298_, BlockPos p_52299_, CollisionContext p_52300_) {
            return SHAPE_BY_AGE[p_52297_.getValue(this.getAgeProperty())];
        }
    });
    public static final RegistryObject<Block> CORN_KERNELS = tag(BlockTags.CROPS).model(Model.Modeled).renderType("cutout").noItem().block("corn_kernels", () -> new DoubleCorpBlock(BlockBehaviour.Properties.copy(Blocks.MELON_STEM)));


    /**Default loots self. Use {@link BlockLootGen#addTables()} to modify.*/



    //definitions
    private static ToIntFunction<BlockState> litBlockEmission(int p_50760_) {
        return (p_50763_) -> {
            return p_50763_.getValue(BlockStateProperties.LIT) ? p_50760_ : 0;
        };
    }
    private static RegistryObject<Block> block(String name){
        return block(name, storedSup);
    }

    private static RegistryObject<Block> block(String name, Supplier<Block> sup){
        RegistryObject<Block> blockObj = BLOCKS.register(name, sup);
        //model
        if (hasItem){
            CreativeModeTab tmpTab = storedTab;
            PVZItems.modelList.add(Pair.of(
                    PVZItems.ITEMS.register(name, () -> new BlockItem(blockObj.get(), storedTab == null ? new Item.Properties() : new Item.Properties().tab(tmpTab))),
                    storedItemModel));
        }
        if (storedModel != Model.Modeled){
            modelList.add(Pair.of(blockObj, Pair.of(storedModel, storedModelTexture)));
        }
        hasItem = true;
        storedModel = Model.Simple;
        storedModelTexture = List.of();
        storedItemModel = Pair.of(PVZItems.Model.Block, new ArrayList<>());
        //flower pot
        if (flowerPot) {
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(Util.prefix(name.substring(7)), blockObj);
            flowerPot = false;
        }
        //tag
        tagMap.put(blockObj, storedTag);
        storedTag = new ArrayList<>();
        //renderType
        if (storedRenderType != null){
            renderTypeMap.put(blockObj, storedRenderType);
            storedRenderType = null;
        }
        //blockEntity
        if (storedBlockEntity != null){
            if (! blockEntityMap.containsKey(storedBlockEntity)){
                blockEntityMap.put(storedBlockEntity, List.of(blockObj));
            } else {
                List<RegistryObject<Block>> list = new ArrayList<>(List.copyOf(blockEntityMap.get(storedBlockEntity)));
                list.add(blockObj);
                blockEntityMap.put(storedBlockEntity, list);
            }
            storedBlockEntity = null;
        }
        //flammable
        if (!storedFlammable.equals(Pair.of(0, 0))){
            flammableMap.put(blockObj, storedFlammable);
            storedFlammable = Pair.of(0, 0);
        }
        //loot
        if (!storedLoot){
            storedLoot = true;
            lootedList.add(blockObj);
        }
        //composter
        if (storedComposterChance > 0) {
            PVZItems.composterMap.put(blockObj, storedComposterChance);
        }
        //return
        return blockObj;
    }

    private static Map<WoodSet, RegistryObject<Block>> wood(String name, AbstractTreeGrower treeGrower, TagKey<Block>... tags){
        /**Boats need to be registered separately in {@link PVZItems}*/
        //init
        Map<WoodSet, RegistryObject<Block>> set = new HashMap<>();
        WoodType woodtype = WoodType.create(name);
        WoodType.register(woodtype);
        woodTypeList.add(woodtype);
        CreativeModeTab tmpTab = storedTab;
        byte flame = 1;
        if (Arrays.stream(tags).toList().contains(BlockTags.NON_FLAMMABLE_WOOD)){
            flame = 0;
        }
        //register
        set.put(WoodSet.Plank, tag(BlockTags.PLANKS).tag(tags).flammable(flame*5, flame*20).block(name+"_planks"));
        set.put(WoodSet.Sapling, tag(BlockTags.SAPLINGS).composter(0.3F).tag(tags).model(Model.Cross).renderType("cutout").itemModel(PVZItems.Model.Simple, res(name+"_sapling")).block(name+"_sapling", () -> new SaplingBlock(treeGrower, BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING))));
        set.put(WoodSet.PottedSapling, tag(BlockTags.FLOWER_POTS).flowerPot().model(Model.Potted, res(name + "_sapling")).renderType("cutout").noItem().block("potted_"+name+"_sapling", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, set.get(WoodSet.Sapling), BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion())));
        set.put(WoodSet.Leaves, tag(BlockTags.LEAVES).composter(0.3F).tag(tags).renderType("cutout").flammable(flame*5, flame*60).loot(false).block(name+"_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES))));
        if (flame == 1) {
            set.put(WoodSet.Wood, tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN).tag(tags).model(Model.Column, res(name + "_log"), res(name + "_log")).flammable(5, 5).block(name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD))));
            set.put(WoodSet.StWood, tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN).tag(tags).model(Model.Column, res("stripped_" + name + "_log"), res("stripped_" + name + "_log")).flammable(5, 5).block("stripped_" + name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD))));
            set.put(WoodSet.Log, tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN).tag(tags).model(Model.Column).flammable(5, 5).block(name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG))));
            set.put(WoodSet.StLog, tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN).tag(tags).model(Model.Column, res("stripped_" + name + "_log"), res(name + "_log_top")).flammable(5, 5).block("stripped_" + name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG))));
        } else {
            set.put(WoodSet.Wood, tag(BlockTags.LOGS).tag(tags).model(Model.Column, res(name + "_log"), res(name + "_log")).block(name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD))));
            set.put(WoodSet.StWood, tag(BlockTags.LOGS).tag(tags).model(Model.Column, res("stripped_" + name + "_log"), res("stripped_" + name + "_log")).block("stripped_" + name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD))));
            set.put(WoodSet.Log, tag(BlockTags.LOGS).tag(tags).model(Model.Column).block(name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG))));
            set.put(WoodSet.StLog, tag(BlockTags.LOGS).tag(tags).model(Model.Column, res("stripped_" + name + "_log"), res(name + "_log_top")).block("stripped_" + name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG))));
        }
        set.put(WoodSet.Slab, tag(BlockTags.WOODEN_SLABS).tag(tags).model(Model.Slab, res(name+"_planks")).flammable(flame*5, flame*5).block(name+"_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB))));
        set.put(WoodSet.Stairs, tag(BlockTags.WOODEN_STAIRS).tag(tags).model(Model.Stairs, res(name+"_planks")).flammable(flame*5, flame*20).block(name+"_stairs", () -> new StairBlock(() -> set.get(WoodSet.Plank).get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS))));
        set.put(WoodSet.Door, tag(BlockTags.WOODEN_DOORS).tag(tags).model(Model.Door).itemModel(PVZItems.Model.Simple).loot(false).block(name+"_door", () -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR))));
        set.put(WoodSet.Trapdoor, tag(BlockTags.WOODEN_TRAPDOORS).tag(tags).model(Model.Trapdoor).itemModel(PVZItems.Model.Block, res(name+"_trapdoor_bottom")).block(name+"_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_TRAPDOOR))));
        set.put(WoodSet.Fence, tag(BlockTags.WOODEN_FENCES).tag(tags).model(Model.Fence, res(name+"_planks")).itemModel(PVZItems.Model.Block, res(name+"_fence_inventory")).flammable(flame*5, flame*20).block(name+"_fence", () -> new FenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE))));
        set.put(WoodSet.Gate, tag(BlockTags.FENCE_GATES).tag(tags).model(Model.Gate, res(name+"_planks")).flammable(flame*5, flame*20).block(name+"_fence_gate", () -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE_GATE))));
        set.put(WoodSet.Button, tag(BlockTags.WOODEN_BUTTONS).tag(tags).model(Model.Button, res(name+"_planks")).itemModel(PVZItems.Model.Block, res(name+"_button_inventory")).block(name+"_button", () -> new WoodButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_BUTTON))));
        set.put(WoodSet.Plate, tag(BlockTags.WOODEN_PRESSURE_PLATES).tag(tags).model(Model.Plate, res(name+"_planks")).block(name+"_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.OAK_PRESSURE_PLATE))));
        set.put(WoodSet.Sign, noItem().tag(BlockTags.STANDING_SIGNS).tag(tags).model(Model.Sign).blockEntity("pvz_sign").block(name+"_sign", () -> new PVZStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN), woodtype)));
        set.put(WoodSet.WallSign, noItem().tag(BlockTags.WALL_SIGNS).tag(tags).model(Model.WallSign, res(name+"_planks")).blockEntity("pvz_sign").block(name+"_wall_sign", () -> new PVZWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN), woodtype)));
        tab(tmpTab);
        PVZItems.modelList.add(Pair.of(
                PVZItems.ITEMS.register(name+"_sign", () -> new SignItem(storedTab == null ? new Item.Properties() : new Item.Properties().tab(tmpTab), set.get(WoodSet.Sign).get(), set.get(WoodSet.WallSign).get())),
                Pair.of(PVZItems.Model.Simple, List.of())));
        woodList.add(set);
        PVZItems.boat(false, woodtype);
        PVZItems.boat(true, woodtype);
        /**Boats added to {@link PVZItems#boatItemMap}.*/
        return set;
    }

    private static PVZBlocks tab(CreativeModeTab tab){
        storedTab = tab;
        return reflector;
    }
    private static PVZBlocks material(Material material){
        storedSup = () -> new Block(BlockBehaviour.Properties.of(material));
        return reflector;
    }
    private static PVZBlocks material(Block block){
        storedSup = () -> new Block(BlockBehaviour.Properties.copy(block));
        return reflector;
    }
    private static PVZBlocks sup(Supplier<Block> sup){
        storedSup = sup;
        return reflector;
    }
    private static PVZBlocks model(Model model){
        storedModel = model;
        storedModelTexture = List.of();
        return reflector;
    }
    private static PVZBlocks composter(float chance){
        storedComposterChance = chance;
        return reflector;
    }
    private static PVZBlocks model(Model model, ResourceLocation... res){
        return model(model, List.of(res));
    }
    private static PVZBlocks model(Model model, List<ResourceLocation> list) {
        storedModel = model;
        storedModelTexture = list;
        return reflector;
    }
    private static PVZBlocks tag(TagKey<Block>... tags){
        return tag(Arrays.asList(tags));
    }
    private static PVZBlocks tag(List<TagKey<Block>> list){
        storedTag.addAll(list);
        return reflector;
    }
    private static ResourceLocation res(String path){
        return new ResourceLocation(PVZMod.MODID, ModelProvider.BLOCK_FOLDER + "/" + path);
    }
    private static PVZBlocks flowerPot() {
        flowerPot = true;
        return reflector;
    }
    private static PVZBlocks noItem(){
        hasItem = false;
        return reflector;
    }
    private static PVZBlocks itemModel(PVZItems.Model model, ResourceLocation... res){
        return itemModel(model, List.of(res));
    }
    private static PVZBlocks itemModel(PVZItems.Model model, List<ResourceLocation> res){
        storedItemModel = Pair.of(model, res);
        return reflector;
    }
    private static PVZBlocks renderType(String type){
        storedRenderType = type;
        return reflector;
    }
    private static PVZBlocks blockEntity(String blockentity){
        storedBlockEntity = blockentity;
        return reflector;
    }
    private static PVZBlocks flammable(Integer ignite, Integer burn){
        storedFlammable = Pair.of(ignite, burn);
        return reflector;
    }
    private static PVZBlocks loot(Boolean loot){
        storedLoot = loot; //false for no auto generation
        return reflector;
    }

    public static void release(){
        List.of(modelList, lootedList).forEach(List::clear);
        List.of(renderTypeMap, tagMap, blockEntityMap).forEach(Map::clear);
    }

    public static void queueRelease() {
        woodList.clear();
        flammableMap.clear();
    }

    public enum WoodSet {
        Plank, Slab, Stairs,
        Door, Trapdoor, Fence, Gate,
        Log, Wood, StLog, StWood,
        Button, Plate, Sign, WallSign,
        Boat, ChestBoat,
        Sapling, PottedSapling, Leaves
    }

    public enum Model {
        Simple, Column, Box, Cross, Carpet,
        Slab, Stairs, Door, Trapdoor,
        Plate, Button, Sign, WallSign,
        Fence, Gate, Potted,
        Modeled
    }
}
