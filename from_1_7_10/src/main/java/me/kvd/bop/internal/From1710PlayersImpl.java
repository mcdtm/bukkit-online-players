package me.kvd.bop.internal;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import me.kvd.bop.Players;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A {@link Players} implementation for servers whose
 * {@code Bukkit#getOnlinePlayers()} returns
 * {@code Collection<? extends Player>}.
 *
 * <p>Covers Minecraft 1.7.10 and later, including all modern forks
 * (Spigot, Paper, Purpur, Folia and others).</p>
 *
 * <p>The class is internal. Consumers obtain a {@link Players} instance
 * through the public API and should not reference this class
 * directly.</p>
 *
 * @since 0.1.0
 */
final class From1710PlayersImpl implements Players {

  From1710PlayersImpl() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public Stream<Player> stream() {
    return (Stream<Player>) Bukkit.getOnlinePlayers().stream();
  }

  @Override
  public Player[] toArray() {
    return Bukkit.getOnlinePlayers().toArray(new Player[0]);
  }

  @Override
  public int count() {
    return Bukkit.getOnlinePlayers().size();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Iterates the collection directly, without allocating a stream or
   * an iterator wrapper.</p>
   */
  @Override
  public void forEach(final Consumer<? super Player> action) {
    Objects.requireNonNull(action, "action");
    for (final var player : Bukkit.getOnlinePlayers()) {
      action.accept(player);
    }
  }
}