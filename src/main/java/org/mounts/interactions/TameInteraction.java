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
            .appendInherited(
                    new KeyedCodec<>("AttachmentOffset", ProtocolCodecs.VECTOR3F),
                    (o, v) -> o.attachmentOffset.assign(v.x, v.y, v.z),
                    o -> new Vector3f(o.attachmentOffset.x, o.attachmentOffset.y, o.attachmentOffset.z),
                    (o, p) -> o.attachmentOffset = p.attachmentOffset
            )
            .add()
            .<MountController>appendInherited(
                    new KeyedCodec<>("Controller", new EnumCodec<>(MountController.class)),
                    (o, v) -> o.controller = v,
                    o -> o.controller,
                    (o, p) -> o.controller = p.controller
            )
            .addValidator(Validators.nonNull())
            .add()
            .build();
    private com.hypixel.hytale.math.vector.Vector3f attachmentOffset = new com.hypixel.hytale.math.vector.Vector3f(0.0F, 0.0F, 0.0F);
    private MountController controller;

    public TameInteraction() {
    }

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        Ref<EntityStore> target = context.getTargetEntity();
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

        //check if the player holds the correct item
        if (target == null) {
            context.getState().state = InteractionState.Failed;
        } else {
            Ref<EntityStore> self = context.getEntity();

            MountedComponent mounted = commandBuffer.getComponent(self, MountedComponent.getComponentType());
            if (mounted != null) {
                commandBuffer.removeComponent(self, MountedComponent.getComponentType());
                context.getState().state = InteractionState.Failed;
            } else {
                MountedByComponent mountedBy = commandBuffer.getComponent(target, MountedByComponent.getComponentType());
                if (mountedBy != null && !mountedBy.getPassengers().isEmpty()) {
                    context.getState().state = InteractionState.Failed;
                } else {
                    commandBuffer.addComponent(self, MountedComponent.getComponentType(), new MountedComponent(target, this.attachmentOffset, this.controller));
                }
            }
        }
    }

}
