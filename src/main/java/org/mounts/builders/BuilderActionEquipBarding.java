package org.mounts.builders;

import com.google.gson.JsonElement;

import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.mounts.actions.ActionEquipBarding;

import javax.annotation.Nonnull;

public class BuilderActionEquipBarding extends BuilderActionBase {

    public BuilderActionEquipBarding() {
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Enable the player to Test the entity";
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

    @Nonnull
    public ActionEquipBarding build(@Nonnull BuilderSupport builderSupport) {
        return new ActionEquipBarding(this, builderSupport);
    }

    @Override
    public Builder<Action> readConfig(@Nonnull JsonElement data) {
        return super.readConfig(data);
    }

}

