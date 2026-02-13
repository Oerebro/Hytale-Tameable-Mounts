package org.mounts.actions;


import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.mounts.builders.BuilderActionEquipBarding;
import org.mounts.components.TameableMountComponent;
import org.mounts.plugin.ChocoboPlugin;
import org.mounts.systems.MountInitSystem;

import javax.annotation.Nonnull;


public class ActionEquipBarding extends ActionBase {

    public ActionEquipBarding(@Nonnull BuilderActionEquipBarding builderActionEquipBarding, @Nonnull BuilderSupport builderSupport) {
        super(builderActionEquipBarding);
    }


    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        System.out.println("Equip barding");
        //get the mount component
        ComponentType<EntityStore, TameableMountComponent> mountComponentType = TameableMountComponent.getComponentType();
        TameableMountComponent mountComponent = store.getComponent(ref, mountComponentType);

        Ref<EntityStore> playerReference = role.getStateSupport().getInteractionIterationTarget();
        Player player = store.getComponent(playerReference, Player.getComponentType());

        //get barding name
        ItemStack itemStack = player.getInventory().getItemInHand();
        String newBarding = itemStack.getItemId();

        ChocoboPlugin.getHytaleLogger().atInfo().log("Barding: "+newBarding);

        assert mountComponent != null;
        if(newBarding.equals(mountComponent.getBarding())) { return false; }
        String oldBarding = mountComponent.getBarding();

        MountInitSystem.setModelAttachment(ref, "Barding", newBarding.replace("_Barding",""), store);

        //modify item stack
        byte slot = player.getInventory().getActiveHotbarSlot();
        ItemStack newItemStack = itemStack.withQuantity(itemStack.getQuantity()-1);
        player.getInventory().getCombinedHotbarFirst().replaceItemStackInSlot(slot,itemStack,newItemStack);

        //return the old barding unless it was default or doesnt exist (null)
        //get a new stack for the old barding
        if(oldBarding == null || oldBarding.equals("Empty")){
            System.out.println("This shouldn't happen: "+oldBarding);
            ChocoboPlugin.getHytaleLogger().atWarning().log("Tried to give a null or default barding!");
            return true;
        }
        System.out.println("Return to player: "+oldBarding);
        ItemStack oldBardingItem = new ItemStack(oldBarding+"_Barding",1);
        player.getInventory().getCombinedEverything().addItemStack(oldBardingItem);

        return true;
    }


}
