package org.mounts.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.NonSerialized;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.mounts.plugin.ChocoboPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
                    .build();


    private boolean isTame = false;
    private int tamingProgress = 0;

    public TameableMountComponent(){
    }

    public TameableMountComponent(boolean isTame, int tamingProgress){
        this.isTame = isTame;
        this.tamingProgress = tamingProgress;
    }

    public TameableMountComponent(TameableMountComponent other){
        this.isTame = other.isTame;
        //this.owner = other.owner;
        this.tamingProgress = other.tamingProgress;
    }

    public void tame(PlayerRef player){
        this.isTame = true;
        //this.owner = player;
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
        if(tamingProgress >= 100){
            isTame = true;
        }
    }

    public boolean isTame(){
        return isTame;
    }

    @Override
    public @Nonnull TameableMountComponent cloneSerializable() {
        return new TameableMountComponent(this);
    }


}
