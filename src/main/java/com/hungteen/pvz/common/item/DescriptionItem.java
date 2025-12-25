package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.PVZPlantConditionMatchingEvent;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.INeedSafeSituation;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
/**This item class contains all the items of pvz mod that has only a description in special.*/
@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class DescriptionItem extends Item {
    String description;
    public DescriptionItem(Properties p_41383_, String description) {
        super(p_41383_);
        this.description = description;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable(description).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, level, tooltip, flagIn);
    }


    //ShellStartupItem

    private static PVZPlantConditionMatchingEvent.OnBlock handlingEvent = null;
    @SubscribeEvent
    public static void shellStartupPlantGoal(PVZPlantConditionMatchingEvent.OnBlock event) {
        if (event.event == null) {
            return;
        }
        if (handlingEvent == null) {
            if (event.phase != PVZPlantConditionMatchingEvent.Phase.POST) {
                return;
            }
            if (event.isPlanting && event.result != null && event.event.spawningEntity instanceof INeedSafeSituation && event.event.getEntity() != null) {
                PVZResourceEvent.CheckPlantConditionEvent resEv = event.event;
                Player player = event.event.getEntity();
                if ((resEv.seedPacket.getItem() instanceof SeedPacketItem<?> item) && item.canBoost()) {
                    //寻找玩家的iCanBePlantedOn的种子包 和 ShellStartup
                    boolean hasShellStartup = false;
                    List<ItemStack> toTestList = new ArrayList<>();
                    List<ItemStack> list = new ArrayList<>(player.getInventory().items);
                    list.add(player.getOffhandItem());
                    for (ItemStack toTest : list) {
                        if (toTest == event.event.seedPacket || toTest.isEmpty()) continue;
                        if (! hasShellStartup && toTest.getItem() == PVZItems.SHELL_STARTUP.get()) {
                            hasShellStartup = true;
                        } else if (toTest.getItem() instanceof SeedPacketItem<?> base) {
                            if (base.canBoost()) {
                                toTestList.add(toTest);
                            }
                        }
                    }
                    if (hasShellStartup && ! toTestList.isEmpty()) {
                        handlingEvent = event;
                        for (ItemStack itemStack : toTestList) {
                            ((SeedPacketItem<?>) itemStack.getItem()).plantOnBlock(event.event.getEntity(), itemStack, event.level, event.pos, event.direction);
                            if (handlingEvent.result == null) {
                                break;
                            }
                        }
                        handlingEvent = null;
                    }
                }
            }
        } else { //处理被内嵌脚本触发的植物
            if (event.phase == PVZPlantConditionMatchingEvent.Phase.PRE) {
                if (event.event.resource.equals(handlingEvent.event.resource)) {
                    event.event.cost += handlingEvent.event.cost;
                }
            } else if (event.result == null) {
                //尝试让handlingEvent的植物种在本植物上
                MutableComponent result = ((INeedSafeSituation) handlingEvent.event.spawningEntity).isVehicleSafe(handlingEvent.event, event.event.spawningEntity, true);
                if (result == null) {
                    if (event.event.resource.equals(handlingEvent.event.resource)) {
                        event.event.cost -= handlingEvent.event.cost;
                    }
                    handlingEvent.result = null;
                } else {
                    event.result = handlingEvent.result; //输出无法种植的原因来表示失败
                }
            }
        }
    }
}
