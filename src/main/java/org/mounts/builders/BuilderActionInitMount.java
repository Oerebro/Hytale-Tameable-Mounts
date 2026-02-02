package org.mounts.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import org.mounts.actions.ActionInitMount;

import javax.annotation.Nonnull;

public class BuilderActionInitMount extends BuilderActionBase {

    public BuilderActionInitMount() {
        System.out.println("BuilderActionInitMount");
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
    public ActionInitMount build(@Nonnull BuilderSupport builderSupport) {
        return new ActionInitMount(this, builderSupport);
    }


}
