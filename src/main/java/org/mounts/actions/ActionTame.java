package org.mounts.actions;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.systems.RoleChangeSystem;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.mounts.builders.BuilderActionTame;
import org.mounts.components.TameableMountComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/*
    The logic for adding tame progress
 */
public class ActionTame extends ActionBase {
    public static final String EMPTY_ROLE_ID = "Empty_Role";
    protected final int emptyRoleIndex;
    private final Random random = new Random();

    public ActionTame(@Nonnull BuilderActionTame builderActionTame, @Nonnull BuilderSupport builderSupport) {
        super(builderActionTame);
        this.emptyRoleIndex = NPCPlugin.get().getIndex("Empty_Role");
    }

    @Override
    public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        Ref<EntityStore> target = role.getStateSupport().getInteractionIterationTarget();
        boolean targetExists = target != null && !store.getArchetype(target).contains(DeathComponent.getComponentType());
        return super.canExecute(ref, role, sensorInfo, dt, store) && targetExists;
    }

    /*
        This is what happens when the tame action is used
     */
    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, role, sensorInfo, dt, store);

        //Interactions interactions = store.getComponent(ref, Interactions.getComponentType());

        //get the mount component
        ComponentType<EntityStore, TameableMountComponent> mountComponentType = TameableMountComponent.getComponentType();
        TameableMountComponent mountComponent = store.getComponent(ref, mountComponentType);

        //increase tame progress by 15-30
        assert mountComponent != null;
        mountComponent.addTameProgress(getAddTameProgress());

        //get player reference in Action context
        Ref<EntityStore> playerReference = role.getStateSupport().getInteractionIterationTarget();
        Player player = store.getComponent(playerReference, Player.getComponentType());

        //get and modify item stack
        ItemStack itemStack = player.getInventory().getItemInHand();
        byte slot = player.getInventory().getActiveHotbarSlot();
        ItemStack newItemStack = itemStack.withQuantity(itemStack.getQuantity()-1);
        player.getInventory().getCombinedHotbarFirst().replaceItemStackInSlot(slot,itemStack,newItemStack);

        //set default particle
        String particleSystem = "Hearts_Subtle";

        if(mountComponent.getTamingProgress() >= 100){
            System.out.println("Role: "+store.getComponent(ref,NPCEntity.getComponentType()).getRoleName());
        }

        if(mountComponent.getTamingProgress() >= 100 && !mountComponent.isTame()){
            particleSystem = "Hearts";
            NPCEntity npc = store.getComponent(ref,NPCEntity.getComponentType());

            String tameRole = npc.getRoleName()+"_Tamed";
            //request a role change to the tamed version of the mount
            RoleChangeSystem.requestRoleChange(ref, npc.getRole(),NPCPlugin.get().getIndex(tameRole), false, store);
            mountComponent.setTame(true);

            setModelAttachment(ref, "Barding", "Saddle",store);
        }

        //get particle spacial info
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        Vector3d pos = transformComponent.getPosition();
        Vector3d position = new Vector3d(pos.x,pos.y+2,pos.z);
        SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
        ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        playerSpatialResource.getSpatialStructure().collect(position, 75.0, results);

        //spawn particle
        ParticleUtil.spawnParticleEffect(particleSystem, position, ref, results, store);
        return true;
    }

    protected  int getAddTameProgress(){
        return random.nextInt(15)+15;
    }

    private static void setModelAttachment( @Nonnull Ref<EntityStore> ref, @Nonnull String slot, @Nullable String attachment, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        if (slot.isEmpty()) {
            throw new IllegalArgumentException("Slot must be specified!");
        } else {
            System.out.println("Test changing attachment");
            ModelComponent modelComponent = componentAccessor.getComponent(ref, ModelComponent.getComponentType());

            assert modelComponent != null;

            NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

            assert npcComponent != null;

            Model model = modelComponent.getModel();
            float scale = model.getScale();
            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(model.getModelAssetId());
            Map<String, String> randomAttachments = model.getRandomAttachmentIds() != null ? new HashMap<>(model.getRandomAttachmentIds()) : new HashMap<>();
            if (attachment != null && !attachment.isEmpty()) {
                randomAttachments.put(slot, attachment);
            } else {
                randomAttachments.remove(slot);
            }

            model = Model.createScaledModel(modelAsset, scale, randomAttachments);
            componentAccessor.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(model));
            Role role = npcComponent.getRole();
            if (role != null) {
                role.updateMotionControllers(ref, model, model.getBoundingBox(), componentAccessor);
            }
        }
    }

    /*
        LovedItems will trigger this Action to instantly 100% tame a mount
     */
    public static class Guaranteed extends ActionTame{
        public Guaranteed(@Nonnull BuilderActionTame builderActionTame, @Nonnull BuilderSupport builderSupport) {
            super(builderActionTame, builderSupport);
        }

        @Override
        protected int getAddTameProgress(){
            return 100;
        }
    }
}
