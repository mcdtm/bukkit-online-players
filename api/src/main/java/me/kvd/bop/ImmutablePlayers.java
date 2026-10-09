package me.kvd.bop;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.bukkit.entity.Player;

/**
 * A {@link Players} implementation backed by an immutable copy of a
 * player list.
 *
 * <p>Instances are detached from Bukkit: no method queries the server, and
 * all operations observe the same data for the lifetime of the instance.
 * Created by {@link Players#snapshot()}.</p>
 *
 * <p>The class is package-private; consumers interact with it only
 * through the {@link Players} interface.</p>
 *
 * @since 0.1.0
 */
final class ImmutablePlayers implements Players {

  private final List<Player> players;

  /**
   * Creates a snapshot from the given collection.
   *
   * <p>The collection is defensively copied and any {@code null} element
   * causes an immediate {@link NullPointerException}.</p>
   *
   * @param players the source players, not {@code null}, no {@code null}
   *                elements
   * @throws NullPointerException if {@code players} is {@code null} or
   *                              contains {@code null}
   * @since 0.1.0
   */
  ImmutablePlayers(final Collection<? extends Player> players) {
    Objects.requireNonNull(players, "players");
    this.players = List.copyOf(players);
  }

  /**
   * Creates a snapshot from the given array.
   *
   * <p>The array is defensively copied and any {@code null} element causes
   * an immediate {@link NullPointerException}.</p>
   *
   * @param players the source players, not {@code null}, no {@code null}
   *                elements
   * @throws NullPointerException if {@code players} is {@code null} or
   *                              contains {@code null}
   * @since 0.1.0
   */
  ImmutablePlayers(final Player[] players) {
    Objects.requireNonNull(players, "players");
    this.players = List.of(players);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns {@code this}. A snapshot is already detached from the
   * source, so capturing it again is a no-op.</p>
   */
  @Override
  public Players snapshot() {
    return this;
  }

  @Override
  public Stream<Player> stream() {
    return this.players.stream();
  }

  @Override
  public Player[] toArray() {
    return this.players.toArray(new Player[0]);
  }

  @Override
  public int count() {
    return this.players.size();
  }

  @Override
  public void forEach(final Consumer<? super Player> action) {
    Objects.requireNonNull(action, "action");
    this.players.forEach(action);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the backing list directly. It is already immutable, so no
   * defensive copy is needed.</p>
   */
  @Override
  public List<Player> toList() {
    return this.players;
  }
}