package pl.gredierowanie.itemshop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ShopOfferRepository {

  private static final String OFFER_QUERY_UUID =
      "SELECT data_for_plugin.status,data_for_plugin.offert_id,offerts.title,offerts.description,offerts.commands "
          + "FROM data_for_plugin,offerts "
          + "WHERE data_for_plugin.offert_id = offerts.id AND data_for_plugin.server_id=? AND data_for_plugin.uuid=?";
  private static final String OFFER_QUERY_NAME =
      "SELECT data_for_plugin.status,data_for_plugin.offert_id,offerts.title,offerts.description,offerts.commands "
          + "FROM data_for_plugin,offerts "
          + "WHERE data_for_plugin.offert_id = offerts.id AND data_for_plugin.server_id=? AND data_for_plugin.player=?";
  private static final String OFFER_STATUS_UPDATE_QUERY = "UPDATE data_for_plugin SET status=? WHERE offert_id=?";
  private static final String UPDATE_SERVER_INFO_QUERY = "UPDATE `servers` SET players_count=?, players_count_max=?, online_list=?, date_online=? WHERE id=?";

  private final Connection connection;

  public ShopOfferRepository(Connection connection) {
    this.connection = connection;
  }

  QueryResponse queryOfferByPlayer(UUID uuid, int serverId) throws SQLException {
    try (PreparedStatement stmt = connection.prepareStatement(OFFER_QUERY_UUID)) {
      stmt.setInt(1, serverId);
      stmt.setString(2, uuid.toString());
      return QueryResponse.deserialize(stmt.executeQuery());
    }
  }

  QueryResponse queryOfferByPlayer(String name, int serverId) throws SQLException {
    try (PreparedStatement stmt = connection.prepareStatement(OFFER_QUERY_NAME)) {
      stmt.setInt(1, serverId);
      stmt.setString(2, name);
      return QueryResponse.deserialize(stmt.executeQuery());
    }
  }

  boolean updateOfferById(int offerId, boolean status) throws SQLException {
    try (PreparedStatement stmt = connection.prepareStatement(OFFER_STATUS_UPDATE_QUERY)) {
      stmt.setBoolean(1, status);
      stmt.setInt(2, offerId);
      return stmt.executeUpdate() > 0;
    }
  }

  boolean updateServerInfo(int serverId, int playerCount, int playerCountMax, List<String> onlinePlayers, long updateTime) throws SQLException{
    try (PreparedStatement stmt = connection.prepareStatement(UPDATE_SERVER_INFO_QUERY)) {
      stmt.setInt(1, playerCount);
      stmt.setInt(2, playerCountMax);
      stmt.setString(3, String.join(",", onlinePlayers));
      stmt.setTimestamp(4, new Timestamp(updateTime));
      stmt.setInt(5, serverId);
      return stmt.executeUpdate() > 0;
    }
  }

  public static record Offer(int id, String title, String description, CommandList commandList) {

  }

  public static class QueryResponse {

    private final Map<Boolean, List<Offer>> offers = new HashMap<>();

    private QueryResponse() {
      offers.put(true, new ArrayList<>());
      offers.put(false, new ArrayList<>());
    }

    public static QueryResponse create() {
      QueryResponse queryResponse = new QueryResponse();
      for (int i = 0; i < 50; i++) {
        boolean asdasd = ThreadLocalRandom.current().nextBoolean();
        addMultimapEntry(queryResponse.offers, asdasd,
            new Offer(i, "Dupiak" + i, "Zajebisty item, wiesz? " + asdasd,
                new CommandList(List.of("say {PLAYER} to sraka"))));
      }
      return queryResponse;
    }

    private static QueryResponse deserialize(ResultSet set) throws SQLException {
      QueryResponse response = new QueryResponse();

      boolean hasEncounteredAny = false;
      while (set.next()) {
        addMultimapEntry(response.offers, set.getBoolean("status"),
            new Offer(set.getInt("offert_id"), set.getString("title"), set.getString("description"),
                new CommandList(Arrays.asList(set.getString("commands").split(",")))));
        hasEncounteredAny = true;
      }

      if (hasEncounteredAny) {
        return response;
      } else {
        return null;
      }
    }

    private static <K, V> void addMultimapEntry(Map<K, List<V>> multimap, K key, V value) {
      multimap.get(key).add(value);
    }

    public List<Offer> getClaimedOffers() {
      return offers.get(true);
    }

    public List<Offer> getUnclaimedOffers() {
      return offers.get(false);
    }
  }
}
