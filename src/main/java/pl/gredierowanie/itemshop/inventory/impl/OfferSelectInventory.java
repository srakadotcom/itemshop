package pl.gredierowanie.itemshop.inventory.impl;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.gredierowanie.itemshop.ShopOfferRepository.QueryResponse;
import pl.gredierowanie.itemshop.inventory.ClickableInventory;

public class OfferSelectInventory implements ClickableInventory {
  private static final ItemStack CLAIMED_ITEM = new ItemStack(Material.MAP);
  private static final ItemStack UNCLAIMED_ITEM = new ItemStack(Material.PAPER);

  static {
    ItemMeta claimedItemMeta = CLAIMED_ITEM.getItemMeta();
    claimedItemMeta.setDisplayName(ChatColor.GREEN + "Odebrane przedmioty");
    claimedItemMeta.setLore(List.of(ChatColor.GRAY + "Kliknij aby zobaczyć listę", ChatColor.GRAY + "odebranych przedmiotów (historię zakupów)"));
    CLAIMED_ITEM.setItemMeta(claimedItemMeta);

    ItemMeta unclaimedItemMeta = UNCLAIMED_ITEM.getItemMeta();
    unclaimedItemMeta.setDisplayName(ChatColor.RED + "Oczekujące przedmioty");
    unclaimedItemMeta.setLore(List.of(ChatColor.GRAY + "Kliknij aby zobaczyć listę", ChatColor.GRAY + "oczekujących (nieodebranych) przedmiotów"));
    UNCLAIMED_ITEM.setItemMeta(unclaimedItemMeta);
  }

  private final QueryResponse response;
  private final boolean canRedeem;

  public OfferSelectInventory(QueryResponse response, boolean canRedeem) {
    this.response = response;
    this.canRedeem = canRedeem;
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    if(event.getRawSlot() == 1) {
      new ItemShopOfferInventory(response.getClaimedOffers(), true, canRedeem).openInventory(event.getWhoClicked());
    } else if(event.getRawSlot() == 3) {
      new ItemShopOfferInventory(response.getUnclaimedOffers(), false, canRedeem).openInventory(event.getWhoClicked());
    }
  }

  @Override
  public Inventory getInventory() {
    Inventory inventory = Bukkit.createInventory(this, InventoryType.HOPPER, "Wybór itemków");
    inventory.setItem(1, CLAIMED_ITEM);
    inventory.setItem(3, UNCLAIMED_ITEM);
    return inventory;
  }
}
