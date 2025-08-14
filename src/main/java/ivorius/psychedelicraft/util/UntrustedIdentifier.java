package ivorius.psychedelicraft.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public final class UntrustedIdentifier {
    private static final Function<String, String> NAMESPACE_CACHE = Util.memoize(domain -> {
        if (!isNamespaceValid(domain)) {
            String newDomain = domain.replaceAll("[^a-z0-9_.-]", "_").replaceAll("^_|_$", "");
            Psychedelicraft.LOGGER.warn("Identifier namespace containing invalid characters had to be corrected. '{}' was changed to '{}'", domain, newDomain);
            return newDomain.intern();
        }
        return domain.intern();
    });
    private static final Function<String, String> PATH_CACHE = Util.memoize(name -> {
        if (!isPathValid(name)) {
            String newName = name.replaceAll("[^a-z0-9/._-]", "_").replaceAll("^_|_$", "");
            Psychedelicraft.LOGGER.warn("Identifier path containing invalid characters had to be corrected. '{}' was changed to '{}'", name, newName);
            return newName.intern();
        }
        return name.intern();
    });
    private static final BiFunction<String, String, Identifier> ID_CACHE = Util.memoize((domain, name) -> {
        int delimiterIndex = name.indexOf(':');
        if (delimiterIndex > -1) {
            domain = name.substring(0, delimiterIndex);
            name = name.substring(delimiterIndex + 1);
        }
        return Identifier.of(NAMESPACE_CACHE.apply(domain), PATH_CACHE.apply(name));
    });

    public static Identifier of(@Nullable String namespace, @Nullable String path) {
        return ID_CACHE.apply((namespace == null ? Identifier.DEFAULT_NAMESPACE : namespace).intern(), (path == null ? "null" : path).intern());
    }

    private static boolean isPathValid(String path) {
        for (int i = 0; i < path.length(); i++) {
            if (!Identifier.isPathCharacterValid(path.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isNamespaceValid(String namespace) {
        for (int i = 0; i < namespace.length(); i++) {
            if (!isNamespaceCharacterValid(namespace.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isNamespaceCharacterValid(char character) {
        return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '.';
    }
}
