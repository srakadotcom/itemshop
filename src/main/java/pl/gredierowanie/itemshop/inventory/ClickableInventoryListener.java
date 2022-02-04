package pl.gredierowanie.itemshop.inventory;

import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public final class ClickableInventoryListener implements Listener {
  @EventHandler
  private void onClick(InventoryClickEvent event) {
    Inventory inventory = event.getView().getTopInventory();
    if(inventory.getHolder() instanceof ClickableInventory holder) {
      event.setResult(Result.DENY);
      holder.onClick(event);
    }
  }
}
