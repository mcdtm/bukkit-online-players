package me.kvd.bop;

import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;
import org.bukkit.entity.Player;

/**
 * A {@link Players} decorator that caches a snapshot for a configured
 * time-to-live.
 *
 * <p>Uses double-checked locking over a single volatile field to keep the
 * fast path (cache hit) lock-free. Refresh happens once, under a lock;
 * concurrent callers within the same TTL window observe the same
 * snapshot.</p>
 *
 * <p>The cache is lazy: there is no background thread. When the TTL
 * expires, the caller that observes the expiry pays the refresh cost.</p>
 *
 * <p>Instances are created by {@link Players#cached(Duration)}. The class
 * is package-private; consumers interact with it only through the
 * {@link Players} interface.</p>
 *
 * @since 0.1.0
 */
final class CachedPlayers implements Players {

  private final Players source;
  private final long ttlNanos;

  /**
   * The current cache entry. Volatile so the fast path reads a fully
   * constructed entry without synchronization. The whole entry is
   * replaced atomically on refresh; there is no partial update.
   */
  private volatile CacheEntry cache;

  /**
   * Creates a caching view over the given source.
   *
   * @param source   the source view, not {@code null}
   * @param ttlNanos the snapshot lifetime in nanoseconds, must be positive
   * @throws NullPointerException     if {@code source} is {@code null}
   * @throws IllegalArgumentException if {@code ttlNanos} is not positive
   * @since 0.1.0
   */
  CachedPlayers(final Players source, final long ttlNanos) {
    this.source = Objects.requireNonNull(source, "source");
    if (0 >= ttlNanos) {
      throw new IllegalArgumentException("ttlNanos must be > 0, was " + ttlNanos);
    }
    this.ttlNanos = ttlNanos;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation streams over the cached snapshot, not over the
   * source. Successive calls within the same TTL window observe the same
   * data.</p>
   */
  @Override
  public Stream<Player> stream() {
    return this.current().stream();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation returns the cached snapshot. Unlike
   * {@link Players#snapshot()} on a plain view, the returned object may
   * be shared across calls within the same TTL window.</p>
   */
  @Override
  public Players snapshot() {
    return this.current();
  }

  @Override
  public int count() {
    return this.current().count();
  }

  /**
   * Returns the current snapshot according to the cache policy.
   *
   * <p>Fast path: reads the volatile field and checks expiry without
   * locking. Slow path: delegates to {@link #refresh()}.</p>
   */
  private Players current() {
    final var entry = this.cache;
    if (null != entry && entry.isNotExpired()) {
      return entry.snapshot();
    }
    return this.refresh();
  }

  /**
   * Refreshes the cache under a lock. The second check inside the lock
   * guards against another thread that already refreshed between the
   * fast-path read and acquiring the monitor.
   */
  private synchronized Players refresh() {
    final var entry = this.cache;
    if (null != entry && entry.isNotExpired()) {
      return entry.snapshot();
    }
    final var fresh = this.source.snapshot();
    this.cache = new CacheEntry(fresh, System.nanoTime() + this.ttlNanos);
    return fresh;
  }

  /**
   * A cache entry: an immutable snapshot together with its expiry
   * instant, measured in {@link System#nanoTime()} ticks.
   */
  private record CacheEntry(Players snapshot, long expiresAtNanos) {

    /**
     * Returns {@code true} if the entry is still valid.
     *
     * <p>Uses {@link System#nanoTime()}, which is monotonic and therefore
     * immune to system clock adjustments.</p>
     */
    boolean isNotExpired() {
      return System.nanoTime() < this.expiresAtNanos;
    }
  }
}