package me.kvd.bop;

import me.kvd.bop.internal.Bootstrap;

/**
 * Static entry point to the shared {@link Players} instance.
 *
 * <p>The instance is created lazily on first access. The version-specific
 * implementation class is loaded at that moment, not when this class is
 * loaded. Subsequent calls are effectively free.</p>
 *
 * <p><strong>Do not call this during plugin class loading or from another
 * plugin's static initializer.</strong> Always call it from
 * {@code onEnable} or later, once the server is fully initialized. The
 * version detection performed by {@link Bootstrap} relies on Bukkit, which
 * is not ready while plugin classes are being loaded.</p>
 *
 * @since 0.1.0
 */
public final class OnlinePlayers {

  /**
   * Prevents instantiation.
   *
   * @throws AssertionError always, to signal that this class is a static
   *                        entry point and must not be instantiated
   * @since 0.1.0
   */
  private OnlinePlayers() {
    throw new AssertionError("No instances.");
  }

  /**
   * Returns the shared {@link Players} instance.
   *
   * <p>The instance is created on the first call and reused afterwards.
   * Safe to call from any thread, but see the class-level note about
   * initialization timing.</p>
   *
   * @return the shared {@link Players} instance, never {@code null}
   * @since 0.1.0
   */
  public static Players get() {
    return Holder.INSTANCE;
  }

  /**
   * Holder idiom. Guarantees thread-safe, lazy initialization: the
   * version-specific implementation class is loaded on the first
   * {@link #get()} call, not when {@link OnlinePlayers} is loaded.
   *
   * @since 0.1.0
   */
  private static final class Holder {

    /**
     * The singleton {@link Players} instance. Initialized once by the
     * class loader.
     */
    static final Players INSTANCE = Bootstrap.create();

    /**
     * Prevents instantiation.
     *
     * @throws AssertionError always
     * @since 0.1.0
     */
    private Holder() {
      throw new AssertionError("No instances.");
    }
  }
}