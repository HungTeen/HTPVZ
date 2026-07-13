package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.common.register.OtherRegisters;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.registries.ForgeRegistries;

public class NutTreeBeeHiveDecorator extends TreeDecorator {
    public static final Codec<NutTreeBeeHiveDecorator> CODEC = Codec.unit(() -> NutTreeBeeHiveDecorator.INSTANCE);
    public static final NutTreeBeeHiveDecorator INSTANCE = new NutTreeBeeHiveDecorator();

    protected TreeDecoratorType<?> type() {
        return OtherRegisters.NUT_TREE_BEEHIVE_DECORATOR.get();
    }

    public void place(Context context) {
        RandomSource random = context.random();
        context.logs().forEach((pos) -> {
            if (random.nextInt(32) > 0) return;
            Direction direction = Direction.fromAxisAndDirection(random.nextBoolean() ? Direction.Axis.Z : Direction.Axis.X
                    , random.nextBoolean() ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
            if (context.isAir(pos.relative(direction))) {
                pos = pos.relative(direction);
                context.setBlock(pos, Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, direction));
                context.level().getBlockEntity(pos, BlockEntityType.BEEHIVE).ifPresent((blockEntity) -> {
                    int beeNum = random.nextInt(2) + 2;
                    for (int i = 0; i < beeNum; ++ i) {
                        CompoundTag compoundtag = new CompoundTag();
                        compoundtag.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(EntityType.BEE).toString());
                        blockEntity.storeBee(compoundtag, random.nextInt(599), false);
                    }

                });
            }
        });
    }
}
