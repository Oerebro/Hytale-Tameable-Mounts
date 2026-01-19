package org.mounts.event;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.plugin.event.PluginEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.mounts.plugin.ChocoboPlugin;

public class AddComponentFromSystemEvent<T extends Component<EntityStore>> extends PluginEvent {
    public Ref<EntityStore> ref;
    public Store<EntityStore> store;
    public ComponentType<EntityStore,T> componentType;


    public AddComponentFromSystemEvent(Ref<EntityStore> ref, Store<EntityStore> store, ComponentType<EntityStore, T> componentType){
        super(ChocoboPlugin.getInstance());

        this.ref = ref;
        this.store = store;
        this.componentType = componentType;
    }
}
