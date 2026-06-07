package edu.group10.monopolydeal.backend.network.protocol;

import edu.group10.monopolydeal.backend.model.card.SimpleCard;
import edu.group10.monopolydeal.backend.model.player.Player;
import java.util.List;
import java.util.Map;

/**
 * Network-safe player state payload composed of simple record types.
 */
public record NetPlayerState(
        Player player,
        List<SimpleCard> hand,
        List<SimpleCard> bank,
        Map<String, List<SimpleCard>> properties,
        Map<String, Integer> houseByColor,
        Map<String, Integer> hotelByColor
) {
}
