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
import pl.gredierowanie.itemshop.inventory.ClickableInventory;

public class ConfirmInventory implements ClickableInventory {

  private final Runnable onConfirm, onDecline;
  private final String action, title;

  public ConfirmInventory(Runnable onConfirm, Runnable onDecline, String action,
      String title) {
    this.onConfirm = onConfirm;
    this.onDecline = onDecline;
    this.action = action;
    this.title = title;
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    if(event.getRawSlot() == 1)
      onConfirm.run();
    else if(event.getRawSlot() == 3)
      onDecline.run();
  }

  @Override
  public Inventory getInventory() {
    Inventory inventory = Bukkit.createInventory(this, InventoryType.HOPPER, title);

    ItemStack confirmItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    ItemStack declineItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);

    ItemMeta confirmItemMeta = confirmItem.getItemMeta();
    confirmItemMeta.setDisplayName(ChatColor.GREEN + "Potwierdź interakcje");
    confirmItemMeta.setLore(List.of(ChatColor.GRAY + "Kliknij aby potwierdzić " + action));
    confirmItem.setItemMeta(confirmItemMeta);

    ItemMeta declineItemMeta = confirmItem.getItemMeta();
    declineItemMeta.setDisplayName(ChatColor.RED + "Anuluj interakcje");
    declineItemMeta.setLore(List.of(ChatColor.GRAY + "Kliknij aby anulować " + action));
    declineItem.setItemMeta(declineItemMeta);

    inventory.setItem(1, confirmItem);
    inventory.setItem(3, declineItem);
    return inventory;
  }
}
