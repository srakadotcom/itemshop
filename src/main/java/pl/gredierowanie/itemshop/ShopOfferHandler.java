package pl.gredierowanie.itemshop;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.Plugin;
import pl.gredierowanie.itemshop.ShopOfferRepository.QueryResponse;

public class ShopOfferHandler {

  private final ShopOfferRepository repository;
  private final Plugin plugin;
  private final HikariDataSource dataSource;

  private final int serverId;

  public ShopOfferHandler(Plugin plugin, String databaseUrl, String databaseUser,
      String databasePassword, int serverId) throws SQLException {
    this.plugin = plugin;
    this.serverId = serverId;

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(databaseUrl);
    config.setUsername(databaseUser);
    config.setPassword(databasePassword);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
    config.addDataSourceProperty("useLocalSessionState", "true");
    config.addDataSourceProperty("rewriteBatchedStatements", "true");
    config.addDataSourceProperty("cacheResultSetMetadata", "true");
    config.addDataSourceProperty("cacheServerConfiguration", "true");
    config.addDataSourceProperty("elideSetAutoCommits", "true");
    config.addDataSourceProperty("maintainTimeStats", "false");

    this.dataSource = new HikariDataSource(config);
    this.repository = new ShopOfferRepository(
        this.dataSource.getConnection());
  }

  public CompletableFuture<QueryResponse> queryAsync(String name) {
    return doAsync(() -> repository.queryOfferByPlayer(name, serverId));
  }

  public CompletableFuture<QueryResponse> queryAsync(UUID uuid) {
    return doAsync(() -> repository.queryOfferByPlayer(uuid, serverId));
  }

  public CompletableFuture<Void> updateAsync(int offerId) {
    return doAsync(() -> {
      if (!repository.updateOfferById(offerId, true)) {
        throw new IllegalArgumentException("No rows were updated! Maybe record does not exist?");
      }
      return null;
    });
  }

  public CompletableFuture<Void> updateServerInfo(int playerCountMax, List<String> onlinePlayers) {
    return doAsync(() -> {
      if (!repository.updateServerInfo(serverId, onlinePlayers.size(), playerCountMax,
          onlinePlayers, System.currentTimeMillis())) {
        throw new IllegalArgumentException("No rows were updated! Maybe record does not exist?");
      }
      return null;
    });
  }

  void shutdown() {
    if (this.dataSource.isRunning()) {
      this.dataSource.close();
    }
  }

  private <T> CompletableFuture<T> doAsync(Callable<T> callable) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
      try {
        completableFuture.complete(callable.call());
      } catch (Exception ex) {
        completableFuture.completeExceptionally(ex);
      }
    });
    return completableFuture;
  }
}
