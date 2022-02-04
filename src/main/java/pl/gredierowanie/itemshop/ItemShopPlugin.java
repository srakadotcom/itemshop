package pl.gredierowanie.itemshop;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import pl.gredierowanie.itemshop.ShopOfferRepository.QueryResponse;
import pl.gredierowanie.itemshop.inventory.ClickableInventoryListener;
import pl.gredierowanie.itemshop.inventory.impl.OfferSelectInventory;

public final class ItemShopPlugin extends JavaPlugin {

  private ShopOfferHandler offerHandler;

  private String errorMessage;
  private String playerNotFound;
  private String itemRedeemed;

  public static ItemShopPlugin getInstance() {
    return ItemShopPlugin.getPlugin(ItemShopPlugin.class);
  }

  private static String fixColor(String message) {
    return ChatColor.translateAlternateColorCodes('&', message);
  }

  @Override
  public void onEnable() {
    saveDefaultConfig();

    this.errorMessage = fixColor(getConfig().getString("messages.internal_error"));
    this.playerNotFound = fixColor(getConfig().getString("messages.player_not_found"));
    this.itemRedeemed = fixColor(getConfig().getString("messages.item_redeemed"));
    int statisticUpdateInterval = getConfig().getInt("statUpdateInterval");

    try {
      this.offerHandler = new ShopOfferHandler(
          this,
          getConfig().getString("database.url"),
          getConfig().getString("database.user"),
          getConfig().getString("database.pass"),
          getConfig().getInt("serverId"));
    } catch (SQLException e) {
      getLogger().severe("Nie udalo sie nawiazac polaczenia z baza danych. Wylaczanie pluginu...");
      getServer().getPluginManager().disablePlugin(this);
      e.printStackTrace();
      return;
    }

    getServer().getPluginManager().registerEvents(new ClickableInventoryListener(), this);
    getServer().getScheduler()
        .runTaskTimer(this, new UpdateTask(), 0, statisticUpdateInterval * 20L);
  }

  @Override
  public void onDisable() {
    offerHandler.shutdown();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      sender.sendMessage("Komenda nie jest dostepna dla konsoli :(");
      return true;
    }

    CompletableFuture<QueryResponse> query;
    boolean canRedeem = true;

    if (sender.hasPermission("itemshop.admin") && args.length == 1) {
      query = offerHandler.queryAsync(args[0]);
      canRedeem = false;
    } else {
      query = offerHandler.queryAsync(player.getUniqueId());
    }

    boolean finalCanRedeem = canRedeem;
    query.whenComplete(
        (queryResponse, throwable) -> {
          if (throwable != null) {
            throwable.printStackTrace();
            sender.sendMessage(errorMessage);
          } else if (queryResponse == null) {
            sender.sendMessage(playerNotFound);
          } else {
            getServer().getScheduler().runTask(this,
                () -> new OfferSelectInventory(queryResponse, finalCanRedeem).openInventory(
                    player));
          }
        });

    return true;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getItemRedeemed() {
    return itemRedeemed;
  }

  public ShopOfferHandler getOfferHandler() {
    return offerHandler;
  }

  public class UpdateTask implements Runnable {

    @Override
    public void run() {
      List<String> onlinePlayers = getServer().getOnlinePlayers().stream()
          .map(Player::getName).toList();
      offerHandler.updateServerInfo(getServer().getMaxPlayers(), onlinePlayers).whenComplete(
          (unused, throwable) -> {
            if (throwable != null) {
              getLogger().severe("Wystapil blad podczas aktualizowania informacji o serwerze!");
              throwable.printStackTrace();
            }
          });
    }
  }
}
