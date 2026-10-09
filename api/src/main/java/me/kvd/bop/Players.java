package me.kvd.bop;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;
import java.util.stream.Stream;
import org.bukkit.entity.Player;

/**
 * Read-only view over the players currently online.
 *
 * <p>Implementations are selected once at first initialization based on the
 * server version. Selection uses neither reflection nor NMS: two precompiled
 * implementations ship alongside each other, and the correct one is loaded
 * lazily on first use.</p>
 *
 * <p><strong>Snapshots.</strong> Every terminal operation (including
 * {@link #stream()}) captures the player set at call time. Later population
 * changes are not visible to that operation. This mirrors the semantics of
 * {@code Bukkit.getOnlinePlayers()} on 1.7.10+.</p>
 *
 * <p><strong>Thread safety.</strong> Implementations are stateless and safe
 * to share. Individual streams are not.</p>
 *
 * @since 0.1.0
 */
public interface Players extends Iterable<Player> {

  /**
   * API version. Incremented on every breaking binary or source change.
   *
   * <p>Consumers should verify compatibility before first use:</p>
   *
   * <pre>{@code
   * if (Players.API_VERSION != 1) {
   *     logger.warning("Incompatible players API version.");
   *     return;
   * }
   * }</pre>
   *
   * @since 0.1.0
   */
  int API_VERSION = 1;

  /**
   * Returns a stream of online players.
   *
   * <p>The stream is backed by a snapshot taken at call time. The stream
   * itself is lazy, but the underlying data does not change between the
   * call and consumption.</p>
   *
   * @return a stream of players, never {@code null}
   * @since 0.1.0
   */
  Stream<Player> stream();

  /**
   * Returns a view whose {@link #stream()} is parallel.
   *
   * <p>Does not copy the underlying data; only marks the stream as parallel.
   * All other methods behave identically to the original view.</p>
   *
   * <p><strong>Note:</strong> parallelism is only worthwhile for
   * computationally expensive operations (I/O, hashing, complex predicates).
   * For simple filtering, the ForkJoinPool overhead usually exceeds the
   * gain.</p>
   *
   * @return a parallel view, never {@code null}
   * @since 0.1.0
   */
  default Players parallel() {
    return new ParallelPlayers(this);
  }

  /**
   * Returns a stream of players matching the predicate.
   *
   * @param filter the predicate, not {@code null}
   * @return a filtered stream
   * @throws NullPointerException if {@code filter} is {@code null}
   * @since 0.1.0
   */
  default Stream<Player> filter(final Predicate<? super Player> filter) {
    Objects.requireNonNull(filter, "filter");
    return this.stream().filter(filter);
  }

  /**
   * Returns a fresh array of online players.
   *
   * @return an array of players, never {@code null}
   * @since 0.1.0
   */
  default Player[] toArray() {
    return this.stream().toArray(Player[]::new);
  }

  /**
   * Returns an immutable list of online players.
   *
   * @return an immutable list, never {@code null}
   * @since 0.1.0
   */
  default List<Player> toList() {
    return this.stream().toList();
  }

  /**
   * Returns the current number of online players.
   *
   * @return the player count, never negative
   * @since 0.1.0
   */
  int count();

  /**
   * Returns {@code true} if no player is online.
   *
   * @return {@code true} if the view is empty
   * @since 0.1.0
   */
  default boolean isEmpty() {
    return 0 == this.count();
  }

  /**
   * Returns {@code true} if at least one player is online.
   *
   * @return {@code true} if the view is not empty
   * @since 0.1.0
   */
  default boolean isPresent() {
    return 0 != this.count();
  }

  /**
   * Performs the action if at least one player is online, otherwise does
   * nothing.
   *
   * @param action the action to perform, not {@code null}
   * @throws NullPointerException if {@code action} is {@code null}
   * @since 0.1.0
   */
  default void ifPresent(final Runnable action) {
    Objects.requireNonNull(action, "action");
    if (this.isPresent()) {
      action.run();
    }
  }

  /**
   * Performs the action with this view if at least one player is online,
   * otherwise does nothing.
   *
   * @param action the action to perform, not {@code null}
   * @throws NullPointerException if {@code action} is {@code null}
   * @since 0.1.0
   */
  default void ifPresent(final Consumer<? super Players> action) {
    Objects.requireNonNull(action, "action");
    if (this.isPresent()) {
      action.accept(this);
    }
  }

  /**
   * Performs the action if no player is online, otherwise does nothing.
   *
   * @param action the action to perform, not {@code null}
   * @throws NullPointerException if {@code action} is {@code null}
   * @since 0.1.0
   */
  default void ifEmpty(final Runnable action) {
    Objects.requireNonNull(action, "action");
    if (this.isEmpty()) {
      action.run();
    }
  }

  /**
   * Returns any online player, if one exists.
   *
   * @return an {@link Optional} containing a player, or empty if none is
   * online
   * @since 0.1.0
   */
  default Optional<Player> any() {
    return this.stream().findAny();
  }

  /**
   * Returns the first online player in iteration order.
   *
   * @return an {@link Optional} containing the first player, or empty if
   * none is online
   * @since 0.1.0
   */
  default Optional<Player> first() {
    return this.stream().findFirst();
  }

  /**
   * Returns the first online player matching the predicate.
   *
   * @param predicate the predicate, not {@code null}
   * @return an {@link Optional} containing the first match, or empty if
   * none matches
   * @throws NullPointerException if {@code predicate} is {@code null}
   * @since 0.1.0
   */
  default Optional<Player> firstMatching(final Predicate<? super Player> predicate) {
    Objects.requireNonNull(predicate, "predicate");
    return this.stream().filter(predicate).findFirst();
  }

  /**
   * Returns any online player matching the predicate.
   *
   * @param predicate the predicate, not {@code null}
   * @return an {@link Optional} containing a match, or empty if none
   * matches
   * @throws NullPointerException if {@code predicate} is {@code null}
   * @since 0.1.0
   */
  default Optional<Player> anyMatching(final Predicate<? super Player> predicate) {
    Objects.requireNonNull(predicate, "predicate");
    return this.stream().filter(predicate).findAny();
  }

  /**
   * Returns all online players whose UUID is contained in the given
   * collection.
   *
   * <p>The result order follows iteration order of the snapshot, not the
   * input order. Uses {@link Set#copyOf(Collection)} for O(1) lookup.</p>
   *
   * @param uniqueIds the UUIDs, not {@code null}, may be empty
   * @return an immutable list of matching players
   * @throws NullPointerException if {@code uniqueIds} is {@code null} or
   *                              contains {@code null}
   * @since 0.1.0
   */
  default List<Player> byUniqueIds(final Collection<UUID> uniqueIds) {
    Objects.requireNonNull(uniqueIds, "uniqueIds");
    if (uniqueIds.isEmpty()) {
      return List.of();
    }
    final var lookup = Set.copyOf(uniqueIds);
    return this.stream()
      .filter(p -> lookup.contains(p.getUniqueId()))
      .toList();
  }

  /**
   * Returns {@code true} if any online player has a UUID contained in the
   * given collection.
   *
   * @param uniqueIds the UUIDs, not {@code null}, may be empty
   * @return {@code true} if at least one match is online
   * @throws NullPointerException if {@code uniqueIds} is {@code null} or
   *                              contains {@code null}
   * @since 0.1.0
   */
  default boolean containsUniqueIds(final Collection<UUID> uniqueIds) {
    Objects.requireNonNull(uniqueIds, "uniqueIds");
    if (uniqueIds.isEmpty()) {
      return false;
    }
    final var lookup = Set.copyOf(uniqueIds);
    return this.stream().anyMatch(p -> lookup.contains(p.getUniqueId()));
  }

  /**
   * Returns all online players with any of the given names.
   *
   * <p>Comparison is case-insensitive. Equivalent to
   * {@link #byNames(Collection, boolean) byNames(names, true)}.</p>
   *
   * @param names the names, not {@code null}, may be empty
   * @return an immutable list of matching players
   * @throws NullPointerException if {@code names} is {@code null} or
   *                              contains {@code null}
   * @since 0.1.0
   */
  default List<Player> byNames(final Collection<String> names) {
    return this.byNames(names, true);
  }

  /**
   * Returns all online players with any of the given names.
   *
   * <p>The result order follows iteration order of the snapshot, not the
   * input order.</p>
   *
   * @param names      the names, not {@code null}, may be empty
   * @param ignoreCase {@code true} for case-insensitive comparison;
   *                   {@code false} for exact matching
   * @return an immutable list of matching players
   * @throws NullPointerException if {@code names} is {@code null} or
   *                              contains {@code null}
   * @since 0.1.0
   */
  default List<Player> byNames(final Collection<String> names, final boolean ignoreCase) {
    Objects.requireNonNull(names, "names");
    if (names.isEmpty()) {
      return List.of();
    }
    if (ignoreCase) {
      final var lookup = names.stream()
        .map(String::toLowerCase)
        .collect(Collectors.toUnmodifiableSet());
      return this.stream()
        .filter(p -> lookup.contains(p.getName().toLowerCase()))
        .toList();
    }
    final var lookup = Set.copyOf(names);
    return this.stream()
      .filter(p -> lookup.contains(p.getName()))
      .toList();
  }

  /**
   * Returns {@code true} if any online player has one of the given names.
   *
   * <p>Comparison is case-insensitive. Equivalent to
   * {@link #containsNames(Collection, boolean) containsNames(names, true)}.</p>
   *
   * @param names the names, not {@code null}, may be empty
   * @return {@code true} if at least one match is online
   * @throws NullPointerException if {@code names} is {@code null} or
   *                              contains {@code null}
   * @since 0.1.0
   */
  default boolean containsNames(final Collection<String> names) {
    return this.containsNames(names, true);
  }

  /**
   * Returns {@code true} if any online player has one of the given names.
   *
   * @param names      the names, not {@code null}, may be empty
   * @param ignoreCase {@code true} for case-insensitive comparison;
   *                   {@code false} for exact matching
   * @return {@code true} if at least one match is online
   * @throws NullPointerException if {@code names} is {@code null} or
   *                              contains {@code null}
   * @since 0.1.0
   */
  default boolean containsNames(final Collection<String> names, final boolean ignoreCase) {
    Objects.requireNonNull(names, "names");
    if (names.isEmpty()) {
      return false;
    }
    if (ignoreCase) {
      final var lookup = names.stream()
        .map(String::toLowerCase)
        .collect(Collectors.toUnmodifiableSet());
      return this.stream().anyMatch(p -> lookup.contains(p.getName().toLowerCase()));
    }
    final var lookup = Set.copyOf(names);
    return this.stream().anyMatch(p -> lookup.contains(p.getName()));
  }

  /**
   * Performs the action for each online player.
   *
   * @param action the action to perform, not {@code null}
   * @throws NullPointerException if {@code action} is {@code null}
   * @since 0.1.0
   */
  @Override
  default void forEach(final Consumer<? super Player> action) {
    Objects.requireNonNull(action, "action");
    this.stream().forEach(action);
  }

  /**
   * Maps each online player and returns an immutable list of the results.
   *
   * @param mapper the mapping function, not {@code null}
   * @param <R>    the result type
   * @return an immutable list of mapped values
   * @throws NullPointerException if {@code mapper} is {@code null}
   * @since 0.1.0
   */
  default <R> List<? extends R> mapToList(final Function<? super Player, ? extends R> mapper) {
    Objects.requireNonNull(mapper, "mapper");
    return this.stream().map(mapper).toList();
  }

  /**
   * Collects online players with the given collector.
   *
   * @param collector the collector, not {@code null}
   * @param <R>       the result type
   * @param <A>       the intermediate accumulation type
   * @return the collection result
   * @throws NullPointerException if {@code collector} is {@code null}
   * @since 0.1.0
   */
  default <R, A> R collect(final Collector<? super Player, A, R> collector) {
    Objects.requireNonNull(collector, "collector");
    return this.stream().collect(collector);
  }

  /**
   * Splits online players into non-overlapping windows of the given size.
   * The last window may be shorter.
   *
   * <pre>{@code
   * players.windowed(10)
   *     .forEach(batch -> batch.forEach(p -> p.sendMessage("...")));
   * }</pre>
   *
   * @param windowSize the window size, must be &gt; 0
   * @return a stream of player lists
   * @throws IllegalArgumentException if {@code windowSize} &lt;= 0
   * @since 0.1.0
   */
  default Stream<List<Player>> windowed(final int windowSize) {
    if (0 >= windowSize) {
      throw new IllegalArgumentException("windowSize must be > 0, was " + windowSize);
    }
    return this.stream().gather(Gatherers.windowFixed(windowSize));
  }

  /**
   * Splits online players into sliding windows of the given size. Each
   * subsequent window drops the oldest element of the previous one and
   * appends a new element.
   *
   * @param windowSize the window size, must be &gt; 0
   * @return a stream of player lists
   * @throws IllegalArgumentException if {@code windowSize} &lt;= 0
   * @since 0.1.0
   */
  default Stream<List<Player>> sliding(final int windowSize) {
    if (0 >= windowSize) {
      throw new IllegalArgumentException("windowSize must be > 0, was " + windowSize);
    }
    return this.stream().gather(Gatherers.windowSliding(windowSize));
  }

  /**
   * Maps each online player concurrently, bounded by the maximum number
   * of parallel tasks, preserving input order.
   *
   * <p>Useful when the mapper performs I/O (database query, HTTP call) and
   * parallelism must be capped to avoid saturating resources.</p>
   *
   * @param maxConcurrency the maximum number of parallel tasks, &gt; 0
   * @param mapper         the mapping function, not {@code null}
   * @param <R>            the result type
   * @return a stream of mapped values in input order
   * @throws IllegalArgumentException if {@code maxConcurrency} &lt;= 0
   * @throws NullPointerException     if {@code mapper} is {@code null}
   * @since 0.1.0
   */
  default <R> Stream<R> mapConcurrent(
    final int maxConcurrency,
    final Function<? super Player, ? extends R> mapper
  ) {
    if (0 >= maxConcurrency) {
      throw new IllegalArgumentException("maxConcurrency must be > 0, was " + maxConcurrency);
    }
    Objects.requireNonNull(mapper, "mapper");
    return this.stream().gather(Gatherers.mapConcurrent(maxConcurrency, mapper));
  }

  /**
   * Folds online players into a single value and emits it as a
   * one-element stream.
   *
   * <p>Unlike {@code Stream#reduce}, the result is a stream, so the
   * pipeline can be continued.</p>
   *
   * @param initial the initial accumulator value
   * @param folder  the combining function, not {@code null}
   * @param <R>     the accumulator type
   * @return a one-element stream containing the folded value
   * @throws NullPointerException if {@code folder} is {@code null}
   * @since 0.1.0
   */
  default <R> Stream<R> fold(
    final R initial,
    final BiFunction<? super R, ? super Player, ? extends R> folder
  ) {
    Objects.requireNonNull(folder, "folder");
    return this.stream().gather(Gatherers.fold(() -> initial, folder));
  }

  /**
   * Scans online players, emitting the intermediate accumulator state
   * after each element. Useful for running totals or counters.
   *
   * @param initial the initial accumulator value
   * @param scanner the combining function, not {@code null}
   * @param <R>     the accumulator type
   * @return a stream of successive accumulator states
   * @throws NullPointerException if {@code scanner} is {@code null}
   * @since 0.1.0
   */
  default <R> Stream<R> scan(
    final R initial,
    final BiFunction<? super R, ? super Player, ? extends R> scanner
  ) {
    Objects.requireNonNull(scanner, "scanner");
    return this.stream().gather(Gatherers.scan(() -> initial, scanner));
  }

  /**
   * Captures the current online players and returns them as a detached,
   * immutable view.
   *
   * <p>After this call, no operation on the returned view queries Bukkit
   * again. Successive calls to {@link #stream()}, {@link #count()} etc.
   * return the same data.</p>
   *
   * @return an immutable snapshot, never {@code null}
   * @since 0.1.0
   */
  default Players snapshot() {
    return new ImmutablePlayers(this.stream().toList());
  }

  /**
   * Returns a view that caches a snapshot for the given time-to-live.
   *
   * <p>The first access within a TTL window creates a snapshot and stores
   * it. Subsequent accesses within the same window return that snapshot
   * without querying Bukkit. After the TTL expires, the next access
   * refreshes it.</p>
   *
   * <p><strong>Thread safety:</strong> the view is safe for concurrent
   * use. Refresh happens once, under a lock; concurrent readers observe
   * the same snapshot.</p>
   *
   * <p><strong>Note:</strong> the cache is lazy — there is no background
   * thread. If the TTL expires on a call, the caller pays the refresh
   * cost (typically a few microseconds).</p>
   *
   * @param ttl the snapshot lifetime, must be positive
   * @return a caching view, never {@code null}
   * @throws NullPointerException     if {@code ttl} is {@code null}
   * @throws IllegalArgumentException if {@code ttl} is zero or negative
   * @since 0.1.0
   */
  default Players cached(final Duration ttl) {
    Objects.requireNonNull(ttl, "ttl");
    if (ttl.isZero() || ttl.isNegative()) {
      throw new IllegalArgumentException("ttl must be positive, was " + ttl);
    }
    return new CachedPlayers(this, ttl.toNanos());
  }

  /**
   * Returns up to {@code count} randomly selected online players in
   * random order.
   *
   * <p>If fewer players are online than {@code count}, returns all of
   * them. Uses reservoir sampling; runs in O(n) regardless of
   * {@code count}.</p>
   *
   * @param count the number of players to select, may be zero or negative
   * @return an immutable list of selected players
   * @since 0.1.0
   */
  default List<Player> random(final int count) {
    return this.random(count, ThreadLocalRandom.current());
  }

  /**
   * Returns up to {@code count} randomly selected online players using
   * the given source of randomness.
   *
   * <p>This overload is intended for deterministic tests.</p>
   *
   * @param count  the number of players to select, may be zero or negative
   * @param random the source of randomness, not {@code null}
   * @return an immutable list of selected players
   * @throws NullPointerException if {@code random} is {@code null}
   * @since 0.1.0
   */
  default List<Player> random(final int count, final Random random) {
    Objects.requireNonNull(random, "random");
    if (0 >= count) {
      return List.of();
    }
    final var all = this.toList();
    if (count >= all.size()) {
      final var copy = new ArrayList<>(all);
      Collections.shuffle(copy, random);
      return List.copyOf(copy);
    }
    final var chosen = new ArrayList<Player>(count);
    for (int i = 0; i < count; i++) {
      chosen.add(all.get(i));
    }
    for (int i = count; i < all.size(); i++) {
      final var j = random.nextInt(i + 1);
      if (j < count) {
        chosen.set(j, all.get(i));
      }
    }
    return List.copyOf(chosen);
  }

  /**
   * Returns an iterator over the snapshot taken at call time.
   *
   * <p>Allows {@code for-each} iteration without breaking snapshot
   * semantics.</p>
   *
   * @return an iterator over online players, never {@code null}
   * @since 0.1.0
   */
  @Override
  default Iterator<Player> iterator() {
    return this.stream().iterator();
  }
}