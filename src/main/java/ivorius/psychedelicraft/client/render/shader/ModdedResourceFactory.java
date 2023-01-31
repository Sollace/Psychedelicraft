package ivorius.psychedelicraft.client.render.shader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;

class ModdedResourceFactory implements ResourceFactory {
    private final ResourceFactory parent;
    private final String defaultNamespace;

    private final Map<Identifier, List<String>> loadedImports = new HashMap<>();

    public ModdedResourceFactory(ResourceFactory parent, String defaultNamespace) {
        this.parent = parent;
        this.defaultNamespace = defaultNamespace;
    }

    @Override
    public Optional<Resource> getResource(Identifier id) {
        final Identifier overrideId = new Identifier(defaultNamespace, id.getPath());
        return parent.getResource(overrideId).map(r -> loadShader(r, overrideId))
             .or(() -> parent.getResource(id).map(r -> loadShader(r, id)));
    }

    private Resource loadShader(Resource resource, Identifier id) {
        if (id.getPath().endsWith(".vsh") || id.getPath().endsWith(".fsh")) {
            return new Resource(resource.getPack(), () -> {
                id.getClass();
                try (var reader = resource.getReader()) {
                    return new ByteArrayInputStream(reader.lines().toList().stream()
                            .flatMap(this::processImport)
                            .collect(ByteArrayOutputStream::new, (buf, line) -> {
                                for (byte b : line.getBytes(StandardCharsets.UTF_8)) {
                                    buf.write(b);
                                }
                                for (byte b : System.lineSeparator().getBytes(StandardCharsets.UTF_8)) {
                                    buf.write(b);
                                }
                            }, (a, b) -> {})
                            .toByteArray());
                }
            }, resource::getMetadata);
        }
        return resource;
    }

    private Stream<String> processImport(String line) {
        if (line.startsWith("#moj_import <") && line.endsWith(">")) {
            Identifier importPath = Identifier.tryParse(line.split("#moj_import <")[1].replace(">", ""));
            if (importPath != null) {
                importPath = importPath.withPrefixedPath("shaders/include/");
                return loadedImports.computeIfAbsent(importPath, p -> {
                    return parent.getResource(p).or(() -> {
                        Psychedelicraft.LOGGER.error("Failed to locate import: {}", line);
                        return Optional.empty();
                    }).stream().filter(Objects::nonNull).flatMap(resource -> {
                        try (var reader = resource.getReader()) {
                            return reader.lines().map(line2 -> line2.startsWith("#version") ? "/* " + line2 + " */" : line2).toList().stream();
                        } catch (IOException e) {
                            return Stream.empty();
                        }
                    }).toList();
                }).stream();
            }
            System.out.println("Failed to locate import: " + line);
            return Stream.of("/*" + line + "*/");
        }
        return Stream.of(line);
    }
}
