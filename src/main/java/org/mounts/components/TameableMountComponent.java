package org.mounts.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.NonSerialized;
import com.hypixel.hytale.protocol.ModelAttachment;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionModelAttachment;
import org.mounts.plugin.ChocoboPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public class TameableMountComponent implements Component<EntityStore> {
    public static final BuilderCodec<TameableMountComponent> CODEC =
            BuilderCodec.builder(TameableMountComponent.class, TameableMountComponent::new)
                    // Encode the isTame field
                    .append(
                            new KeyedCodec<>("IsTame", Codec.BOOLEAN),
                            (mountComponent, isTame) -> mountComponent.isTame = isTame,  // set the field directly
                            mountComponent -> mountComponent.isTame                       // get the field
                    )
                    .add()
                    // Encode the tamingProgress field
                    .append(
                            new KeyedCodec<>("TamingProgress", Codec.INTEGER),
                            (mountComponent, progress) -> mountComponent.tamingProgress = progress,
                            mountComponent -> mountComponent.tamingProgress
                    )
                    .add()
                    .append(
                            new KeyedCodec<>("Saddle", Codec.STRING),
                            (mountComponent, saddle) -> mountComponent.saddle = saddle,
                            mountComponent -> mountComponent.saddle
                    )
                    .add()
                    .append(
                            new KeyedCodec<>("Barding", Codec.STRING),
                            (mountComponent, barding) -> mountComponent.barding = barding,
                            mountComponent -> mountComponent.barding
                    )
                    .add()
                    .build();


    private boolean isTame = false;
    private int tamingProgress = 0;
    private String saddle = null;
    private String barding = null;
    private String gradientSet = null;
    private String gradientId = null;

    public TameableMountComponent(){
    }

    public TameableMountComponent(boolean isTame, int tamingProgress){
        this.isTame = isTame;
        this.tamingProgress = tamingProgress;
    }

    public TameableMountComponent(TameableMountComponent other){
        this.isTame = other.isTame;
        this.tamingProgress = other.tamingProgress;
        this.saddle = other.saddle;
        this.barding = other.barding;
        this.gradientId = other.gradientId;
        this.gradientSet = other.gradientSet;
    }

    public String getSaddle(){
        return this.saddle;
    }

    public String getBarding(){
        return this.barding;
    }

    public String getGradientSet(){
        return this.gradientSet;
    }

    public String getGradientId(){
        return this.gradientId;
    }

    public void setGradientSet(String gradientSet){
        this.gradientSet = gradientSet;
    }

    public void setGradientId(String gradientId){
        this.gradientId = gradientId;
    }

    public void setAttachment(String slot, String attachment) {
        switch (slot) {
            case "Barding" -> this.barding = attachment;
            case "Saddle"  -> this.saddle = attachment;
            default -> throw new IllegalArgumentException("Unknown slot: " + slot);
        }
    }

    @Nullable
    @Override
    public TameableMountComponent clone(){
        return new TameableMountComponent(this);
    }

    public int getTamingProgress(){
        return tamingProgress;
    }


    public static ComponentType<EntityStore,TameableMountComponent> getComponentType(){
        return ChocoboPlugin.getInstance().getTameableMountComponentComponentType();
    }

    public void addTameProgress(int progress){
        this.tamingProgress += progress;
    }

    public void setTame(boolean tame){
        this.isTame = tame;
    }

    public boolean isTame(){
        return isTame;
    }

    @Override
    public @Nonnull TameableMountComponent cloneSerializable() {
        return new TameableMountComponent(this);
    }


}
