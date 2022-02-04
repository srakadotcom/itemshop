package pl.gredierowanie.itemshop.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface ClickableInventory extends InventoryHolder {
  void onClick(InventoryClickEvent event);

  default void openInventory(HumanEntity entity) {
    entity.openInventory(getInventory());
  }
}
