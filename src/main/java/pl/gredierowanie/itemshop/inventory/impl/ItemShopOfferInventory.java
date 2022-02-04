package pl.gredierowanie.itemshop.inventory.impl;

import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.gredierowanie.itemshop.ItemShopPlugin;
import pl.gredierowanie.itemshop.ShopOfferRepository.Offer;
import pl.gredierowanie.itemshop.inventory.ClickableInventory;

public class ItemShopOfferInventory implements ClickableInventory {

  private static final int PAGE_SIZE = 36;
  private static final ItemStack NEXT_PAGE = new ItemStack(Material.LIGHT_BLUE_WOOL);
  private static final ItemStack PREVIOUS_PAGE = new ItemStack(Material.BLUE_WOOL);

  static {
    ItemMeta nextPageItemMeta = NEXT_PAGE.getItemMeta();
    nextPageItemMeta.setDisplayName(ChatColor.GOLD + "Nastepna strona");
    nextPageItemMeta.setLore(List.of(
        ChatColor.GRAY + "Kliknij aby przejsc na nastepna strone listy."
    ));
    NEXT_PAGE.setItemMeta(nextPageItemMeta);

    ItemMeta previousPageItemMeta = PREVIOUS_PAGE.getItemMeta();
    previousPageItemMeta.setDisplayName(ChatColor.GOLD + "Poprzednia strona");
    previousPageItemMeta.setLore(List.of(
        ChatColor.GRAY + "Kliknij aby przejsc na poprzednia strone listy."
    ));
    PREVIOUS_PAGE.setItemMeta(previousPageItemMeta);
  }

  private final List<Offer> offers;
  private final int page;
  private final boolean redeemedOfferInventory, canRedeem;

  public ItemShopOfferInventory(
      List<Offer> offers, boolean redeemedOfferInventory, boolean canRedeem) {
    this(offers, 0, redeemedOfferInventory, canRedeem);
  }

  private ItemShopOfferInventory(
      List<Offer> offers, int page, boolean redeemedOfferInventory, boolean canRedeem) {
    this.offers = offers;
    this.page = page;
    this.redeemedOfferInventory = redeemedOfferInventory;
    this.canRedeem = canRedeem;
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    if (redeemedOfferInventory) {
      if (event.getRawSlot() == 36) {
        if (page > 0) {
          new ItemShopOfferInventory(offers, page - 1, false, true).openInventory(
              event.getWhoClicked());
        }
      } else if (event.getRawSlot() == 44) {
        if (offers.size() > getCurrentPageSize() + PAGE_SIZE) {
          new ItemShopOfferInventory(offers, page + 1, false, true).openInventory(
              event.getWhoClicked());
        }
      }
    } else {
      if(!canRedeem) {
        return;
      }

      int slot = getCurrentPageSize() + event.getRawSlot();
      if (slot >= offers.size() || event.getRawSlot() >= PAGE_SIZE) {
        return;
      }

      Offer offer = offers.get(slot);
      new ConfirmInventory(() -> {
        event.getWhoClicked().closeInventory();
        ItemShopPlugin.getInstance().getOfferHandler().updateAsync(offer.id()).whenComplete(
            (unused, throwable) -> {
              if (throwable == null) {
                ItemShopPlugin.getInstance().getServer().getScheduler()
                        .runTask(ItemShopPlugin.getInstance(),
                            () -> offer.commandList().execute(event.getWhoClicked()));
                event.getWhoClicked()
                    .sendMessage(ItemShopPlugin.getInstance().getItemRedeemed());
              } else {
                throwable.printStackTrace();
                event.getWhoClicked().sendMessage(ItemShopPlugin.getInstance().getErrorMessage());
              }
            });
      }, () -> openInventory(event.getWhoClicked()), "odbiór przedmiotu",
          "Potwierdzenie odebrania " + offer.title())
          .openInventory(event.getWhoClicked());
    }
  }

  @Override
  public Inventory getInventory() {
    Inventory inventory = Bukkit.createInventory(this, PAGE_SIZE + 9,
        "Oferty " + (redeemedOfferInventory ? "odebrane" : "nieodebrane") + " - strona " + (page
            + 1));

    for (int slot = 0; slot < PAGE_SIZE; slot++) {
      int offerSlot = getCurrentPageSize() + slot;
      if (offerSlot >= offers.size()) {
        break;
      }
      Offer offer = offers.get(offerSlot);

      ItemStack offerItem = new ItemStack(Material.PAPER);
      ItemMeta offerItemMeta = offerItem.getItemMeta();
      offerItemMeta.setDisplayName(ChatColor.GRAY + offer.title());
      offerItemMeta.setLore(Collections.singletonList(ChatColor.GRAY + offer.description()));
      offerItem.setItemMeta(offerItemMeta);

      inventory.setItem(slot, offerItem);
    }

    if (page > 0) {
      inventory.setItem(36, PREVIOUS_PAGE);
    }

    if (offers.size() > getCurrentPageSize() + PAGE_SIZE) {
      inventory.setItem(44, NEXT_PAGE);
    }
    return inventory;
  }

  private int getCurrentPageSize() {
    return page * PAGE_SIZE;
  }
}
