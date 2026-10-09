package me.kvd.bop.internal;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import me.kvd.bop.Players;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A {@link Players} implementation for servers whose
 * {@code Bukkit#getOnlinePlayers()} returns {@code Player[]}.
 *
 * <p>Covers Minecraft 1.7.2 through 1.7.9 inclusive. The class is
 * compiled against the Bukkit 1.7.2 API, whose
 * {@code Bukkit#getOnlinePlayers()} returns an array. On 1.7.10 and
 * later, the array-returning overload was removed; loading this class
 * on such a server fails on the first call, not on class
 * load.</p>
 *
 * <p>The class is internal. Consumers obtain a {@link Players} instance
 * through the public API and should not reference this class
 * directly.</p>
 *
 * @since 0.1.0
 */
final class Pre1710PlayersImpl implements Players {

  Pre1710PlayersImpl() {
  }

  @Override
  public Stream<Player> stream() {
    return Arrays.stream(Bukkit.getOnlinePlayers());
  }

  @Override
  public Player[] toArray() {
    return Bukkit.getOnlinePlayers();
  }

  @Override
  public int count() {
    return Bukkit.getOnlinePlayers().length;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Iterates the array directly, without allocating a stream or an
   * iterator wrapper.</p>
   */
  @Override
  public void forEach(final Consumer<? super Player> action) {
    Objects.requireNonNull(action, "action");
    for (final var player : Bukkit.getOnlinePlayers()) {
      action.accept(player);
    }
  }
}