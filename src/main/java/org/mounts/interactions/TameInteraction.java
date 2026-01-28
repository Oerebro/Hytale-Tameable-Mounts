package org.mounts.interactions;


import com.hypixel.hytale.builtin.mounts.MountedByComponent;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MountController;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TameInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<TameInteraction> CODEC = BuilderCodec.builder(
                    TameInteraction.class, TameInteraction::new, SimpleInstantInteraction.CODEC
            )
            .build();
    private com.hypixel.hytale.math.vector.Vector3f attachmentOffset = new com.hypixel.hytale.math.vector.Vector3f(0.0F, 0.0F, 0.0F);
    private MountController controller;

    public TameInteraction() {
    }

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        Ref<EntityStore> target = context.getTargetEntity();
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        System.out.println("Test to see where this is triggered!");
        if (target == null) {
            context.getState().state = InteractionState.Failed;
        } else {
            //Ref<EntityStore> origin = context.getEntity();
            //Player player = commandBuffer.getComponent(origin,Player.getComponentType());

            //System.out.println(player.getInventory().getItemInHand().getItem().getTranslationProperties().getName());
            //NPCEntity mount = commandBuffer.getComponent(target, NPCEntity.getComponentType());
        }

    }

}
