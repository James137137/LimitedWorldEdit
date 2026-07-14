package com.james137137.limitedworldedit;

import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Adds read protection at the World level. WorldEdit intentionally bypasses
 * its extent chain for some source operations, including clipboard copies.
 */
final class LimitedWorlds {

    private LimitedWorlds() {
    }

    static World wrap(World delegate, OwnedRegionMask mask, boolean restoreAsOverride) {
        Objects.requireNonNull(delegate, "delegate");
        Objects.requireNonNull(mask, "mask");

        BlockState air = Objects.requireNonNull(BlockTypes.AIR).getDefaultState();
        BiomeType plains = Objects.requireNonNull(BiomeTypes.PLAINS);
        return wrap(delegate, mask, restoreAsOverride, air, air.toBaseBlock(), plains);
    }

    static World wrap(World delegate, OwnedRegionMask mask, boolean restoreAsOverride,
                      BlockState outsideBlockState, BaseBlock outsideFullBlock, BiomeType outsideBiome) {
        LimitedWorldHandler handler = new LimitedWorldHandler(
                Objects.requireNonNull(delegate, "delegate"),
                Objects.requireNonNull(mask, "mask"),
                restoreAsOverride,
                Objects.requireNonNull(outsideBlockState, "outsideBlockState"),
                Objects.requireNonNull(outsideFullBlock, "outsideFullBlock"),
                Objects.requireNonNull(outsideBiome, "outsideBiome"));
        return (World) Proxy.newProxyInstance(
                LimitedWorlds.class.getClassLoader(),
                new Class<?>[]{World.class, LimitedWorldView.class},
                handler);
    }

    static boolean isLimited(World world) {
        return world instanceof LimitedWorldView;
    }

    static World unwrap(World world) {
        return world instanceof LimitedWorldView view ? view.limitedWorldEditDelegate() : world;
    }

    static World restoreValue(World world) {
        if (!(world instanceof LimitedWorldView view)) {
            return world;
        }
        return view.limitedWorldEditRestoreAsOverride() ? view.limitedWorldEditDelegate() : null;
    }

    static OwnedRegionMask mask(World world) {
        return world instanceof LimitedWorldView view ? view.limitedWorldEditMask() : null;
    }

    private interface LimitedWorldView {

        World limitedWorldEditDelegate();

        OwnedRegionMask limitedWorldEditMask();

        boolean limitedWorldEditRestoreAsOverride();
    }

    private static final class LimitedWorldHandler implements InvocationHandler {

        private final World delegate;
        private final OwnedRegionMask mask;
        private final boolean restoreAsOverride;
        private final BlockState outsideBlockState;
        private final BaseBlock outsideFullBlock;
        private final BiomeType outsideBiome;

        private LimitedWorldHandler(World delegate, OwnedRegionMask mask, boolean restoreAsOverride,
                                    BlockState outsideBlockState, BaseBlock outsideFullBlock,
                                    BiomeType outsideBiome) {
            this.delegate = delegate;
            this.mask = mask;
            this.restoreAsOverride = restoreAsOverride;
            this.outsideBlockState = outsideBlockState;
            this.outsideFullBlock = outsideFullBlock;
            this.outsideBiome = outsideBiome;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
            String name = method.getName();

            if (method.getDeclaringClass() == LimitedWorldView.class) {
                return switch (name) {
                    case "limitedWorldEditDelegate" -> delegate;
                    case "limitedWorldEditMask" -> mask;
                    case "limitedWorldEditRestoreAsOverride" -> restoreAsOverride;
                    default -> throw new IllegalStateException("Unknown limited-world method: " + name);
                };
            }
            if (method.getDeclaringClass() == Object.class) {
                return invokeObjectMethod(proxy, name, arguments);
            }

            BlockVector3 position = firstArgument(arguments, BlockVector3.class);
            if (position != null && !mask.contains(position)) {
                return outsideResult(name, method.getReturnType());
            }

            if (name.equals("createEntity")) {
                Location location = firstArgument(arguments, Location.class);
                if (location != null && !mask.contains(location.toVector().toBlockPoint())) {
                    return null;
                }
            }

            Object result = invokeDelegate(method, arguments);
            if (name.equals("getEntities") && result instanceof List<?> entities) {
                return entities.stream()
                        .filter(Entity.class::isInstance)
                        .map(Entity.class::cast)
                        .filter(entity -> mask.contains(entity.getLocation().toVector().toBlockPoint()))
                        .toList();
            }
            return result;
        }

        private Object outsideResult(String name, Class<?> returnType) {
            return switch (name) {
                case "getBlock" -> outsideBlockState;
                case "getFullBlock" -> outsideFullBlock;
                case "getBiome" -> outsideBiome;
                case "getBlockLightLevel" -> 0;
                case "applySideEffects" -> Set.of();
                case "simulateBlockMine" -> null;
                default -> defaultValue(returnType);
            };
        }

        private Object invokeObjectMethod(Object proxy, String name, Object[] arguments) {
            return switch (name) {
                case "equals" -> {
                    Object other = arguments[0];
                    if (other instanceof LimitedWorldView view) {
                        other = view.limitedWorldEditDelegate();
                    }
                    yield proxy == arguments[0] || delegate.equals(other);
                }
                case "hashCode" -> delegate.hashCode();
                case "toString" -> "LimitedWorld[" + delegate + "]";
                default -> throw new IllegalStateException("Unknown Object method: " + name);
            };
        }

        private Object invokeDelegate(Method method, Object[] arguments) throws Throwable {
            try {
                return method.invoke(delegate, arguments);
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        }

        private static Object defaultValue(Class<?> type) {
            if (!type.isPrimitive()) {
                return null;
            }
            if (type == boolean.class) {
                return false;
            }
            if (type == char.class) {
                return '\0';
            }
            if (type == float.class) {
                return 0.0F;
            }
            if (type == double.class) {
                return 0.0D;
            }
            return 0;
        }

        private static <T> T firstArgument(Object[] arguments, Class<T> type) {
            if (arguments == null) {
                return null;
            }
            for (Object argument : arguments) {
                if (type.isInstance(argument)) {
                    return type.cast(argument);
                }
            }
            return null;
        }
    }
}
