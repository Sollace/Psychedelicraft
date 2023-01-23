package ivorius.psychedelicraft.client.render.shader;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.resource.*;
import net.minecraft.util.Identifier;

class ModdedResourceManager implements ResourceManager {
    private final ResourceManager parent;
    private final String defaultNamespace;
    private final ModdedResourceFactory proxy;

    public ModdedResourceManager(ResourceManager parent, String defaultNamespace) {
        this.parent = parent;
        this.defaultNamespace = defaultNamespace;
        this.proxy = new ModdedResourceFactory(parent, defaultNamespace);
    }

    @Override
    public Optional<Resource> getResource(Identifier id) {
        return proxy.getResource(id);
    }

    @Override
    public Set<String> getAllNamespaces() {
        return parent.getAllNamespaces();
    }

    @Override
    public List<Resource> getAllResources(Identifier id) {
        List<Resource> resources = parent.getAllResources(new Identifier(defaultNamespace, id.getPath()));
        if (resources.isEmpty()) {
            return parent.getAllResources(id);
        }
        return resources;
    }

    @Override
    public Map<Identifier, Resource> findResources(String path, Predicate<Identifier> filter) {
        return parent.findResources(path, filter);
    }

    @Override
    public Map<Identifier, List<Resource>> findAllResources(String path, Predicate<Identifier> filter) {
        return parent.findAllResources(path, filter);
    }

    @Override
    public Stream<ResourcePack> streamResourcePacks() {
        return parent.streamResourcePacks();
    }
}
