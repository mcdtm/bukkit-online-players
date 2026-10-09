package me.kvd.bop;

import java.util.Objects;
import java.util.stream.Stream;
import org.bukkit.entity.Player;

/**
 * A {@link Players} decorator that marks the source stream as parallel.
 *
 * <p>All methods delegate to the wrapped view. Only {@link #stream()}
 * changes behavior: it returns a parallel stream instead of a sequential
 * one. Terminal operations that gain nothing from parallelism
 * ({@link #count()}, {@link #toArray()}) are forwarded directly to the
 * delegate to avoid ForkJoinPool overhead.</p>
 *
 * <p>Instances are created by {@link Players#parallel()}. The class is
 * package-private; consumers interact with it only through the
 * {@link Players} interface.</p>
 *
 * @since 0.1.0
 */
final class ParallelPlayers implements Players {

  private final Players delegate;

  /**
   * Creates a parallel view over the given delegate.
   *
   * @param delegate the source view, not {@code null}
   * @throws NullPointerException if {@code delegate} is {@code null}
   * @since 0.1.0
   */
  ParallelPlayers(final Players delegate) {
    this.delegate = Objects.requireNonNull(delegate, "delegate");
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation returns a parallel stream. The order of
   * elements observed by terminal operations that do not preserve
   * encounter order (e.g. {@link Stream#forEach}) is unspecified.</p>
   */
  @Override
  public Stream<Player> stream() {
    return this.delegate.stream().parallel();
  }

  @Override
  public int count() {
    return this.delegate.count();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation delegates directly to the wrapped view.
   * Parallelism is irrelevant for a snapshot, and the default
   * implementation would otherwise collect through a parallel stream,
   * paying the ForkJoinPool cost for no gain.</p>
   */
  @Override
  public Players snapshot() {
    return this.delegate.snapshot();
  }
}