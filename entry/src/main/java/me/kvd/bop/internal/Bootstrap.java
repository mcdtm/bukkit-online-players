package me.kvd.bop.internal;

import me.kvd.bop.Players;
import me.kvdpxne.notchity.MinecraftVersionCreator;

/**
 * Internal bootstrap that selects the {@link Players} implementation
 * matching the running server version.
 *
 * <p>Selection is based on the server version string and uses neither
 * reflection nor NMS. Both implementation classes are present in the
 * classpath, but only one is ever instantiated. The other is never
 * touched, so its potentially incompatible bytecode references are never
 * resolved by the JVM.</p>
 *
 * <p>This class is internal. Consumers obtain a {@link Players} instance
 * through the public API; they should not reference this class
 * directly.</p>
 *
 * @since 0.1.0
 */
public final class Bootstrap {

  /**
   * First Minecraft version whose Bukkit API returns a
   * {@code Collection<? extends Player>} from
   * {@code Bukkit#getOnlinePlayers()} rather than a {@code Player[]}.
   *
   * <p>Encoded as {@code major * 1_000_000 + minor * 1_000 + patch} to
   * match the format used by {@link MinecraftVersionCreator}.</p>
   */
  private static final int VERSION_1_7_10 = 1_007_010;

  /**
   * Prevents instantiation.
   *
   * @throws AssertionError always, to signal that this class is a utility
   *                        holder and must not be instantiated
   * @since 0.1.0
   */
  private Bootstrap() {
    throw new AssertionError("No instances.");
  }

  /**
   * Creates the {@link Players} implementation matching the running
   * server version.
   *
   * <p>Intended to be called exactly once during plugin startup and cached
   * by the caller. Do not call this method on every access; the version
   * detection and implementation selection are not free.</p>
   *
   * <p>On Minecraft 1.7.10 and later, returns an implementation backed by
   * {@code Collection<? extends Player>}. On earlier versions, returns an
   * implementation backed by {@code Player[]}.</p>
   *
   * @return a {@link Players} instance, never {@code null}
   * @since 0.1.0
   */
  public static Players create() {
    return MinecraftVersionCreator.getMinecraftVersion().isLaterThanOrEqual(VERSION_1_7_10)
      ? new From1710PlayersImpl()
      : new Pre1710PlayersImpl();
  }
}