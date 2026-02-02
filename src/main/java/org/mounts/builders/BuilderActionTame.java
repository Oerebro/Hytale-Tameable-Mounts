package org.mounts.builders;

import com.google.gson.JsonElement;

import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.mounts.actions.ActionTame;

import javax.annotation.Nonnull;

public class BuilderActionTame extends BuilderActionBase {
    protected final FloatHolder anchorX = new FloatHolder();
    protected final FloatHolder anchorY = new FloatHolder();
    protected final FloatHolder anchorZ = new FloatHolder();
    protected final StringHolder movementConfig = new StringHolder();

    public BuilderActionTame() {

    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Enable the player to tame the entity";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return this.getShortDescription();
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    public float getAnchorX(@Nonnull BuilderSupport support) {
        return this.anchorX.get(support.getExecutionContext());
    }

    public float getAnchorY(@Nonnull BuilderSupport support) {
        return this.anchorY.get(support.getExecutionContext());
    }

    public float getAnchorZ(@Nonnull BuilderSupport support) {
        return this.anchorZ.get(support.getExecutionContext());
    }

    public String getMovementConfig(@Nonnull BuilderSupport support) {
        return this.movementConfig.get(support.getExecutionContext());
    }

    @Nonnull
    public ActionTame build(@Nonnull BuilderSupport builderSupport) {
        return new ActionTame(this, builderSupport);
    }

    @Override
    public Builder<Action> readConfig(@Nonnull JsonElement data) {
        return super.readConfig(data);
    }

    public static class Guaranteed extends  BuilderActionTame{
        @Nonnull
        public ActionTame build(@Nonnull BuilderSupport builderSupport) {
            return new ActionTame.Guaranteed(this, builderSupport);
        }
    }
}
