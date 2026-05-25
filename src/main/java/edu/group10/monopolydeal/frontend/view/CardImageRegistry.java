package edu.group10.monopolydeal.frontend.view;

import edu.group10.monopolydeal.backend.model.card.Card;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class CardImageRegistry {
    private static final String CARD_IMG_BASE = "/images/cards/";
    private static final Map<String, String> CARD_NAME_TO_FILE = Map.ofEntries(
            Map.entry("Pass Go", "passgo.jpg"),
            Map.entry("Double The Rent", "doubletherent.jpg"),
            Map.entry("Just Say No", "justsayno.jpg"),
            Map.entry("Hotel", "hotel.jpg"),
            Map.entry("House", "house.jpg"),
            Map.entry("Deal Breaker", "dealbreaker.jpg"),
            Map.entry("Sly Deal", "slydeal.jpg"),
            Map.entry("Forced Deal", "forceddeal.jpg"),
            Map.entry("It's My Birthday", "itsmybirthday.jpg"),
            Map.entry("Debt Collector", "debtcollector.jpg"),
            Map.entry("Rent Wild", "rentwild.jpg"),
            Map.entry("Rent Light Blue-Brown", "rentlbbr.jpg"),
            Map.entry("Rent Orange-Pink", "rentop.jpg"),
            Map.entry("Rent Yellow-Red", "rentry.jpg"),
            Map.entry("Rent Utility-Railroad", "rentut.jpg"),
            Map.entry("Rent Blue-Green", "rentbg.jpg"),
            Map.entry("Ten Million", "10million.jpg"),
            Map.entry("One Million", "1million.jpg"),
            Map.entry("Two Million", "2million.jpg"),
            Map.entry("Three Million", "3million.jpg"),
            Map.entry("Four Million", "4million.jpg"),
            Map.entry("Five Million", "5million.jpg"),
            Map.entry("Atlantic Avenue", "atlanticavenue.jpg"),
            Map.entry("Baltic Avenue", "balticavenue.jpg"),
            Map.entry("B & O Railroad", "bandorailroad.jpg"),
            Map.entry("Boardwalk", "boardwalk.jpg"),
            Map.entry("Connecticut Avenue", "connecticutavenue.jpg"),
            Map.entry("Electric Company", "electriccompany.jpg"),
            Map.entry("Illinois Avenue", "illinoisavenue.jpg"),
            Map.entry("Indiana Avenue", "indianaavenue.jpg"),
            Map.entry("Kentucky Avenue", "kentuckyavenue.jpg"),
            Map.entry("Marvin Gardens", "marvingardens.jpg"),
            Map.entry("Mediterranean Avenue", "mediterraneanavenue.jpg"),
            Map.entry("New York Avenue", "newyorkavenue.jpg"),
            Map.entry("North Carolina Avenue", "northcarolinaavenue.jpg"),
            Map.entry("Oriental Avenue", "orientalavenue.jpg"),
            Map.entry("Pacific Avenue", "pacificavenue.jpg"),
            Map.entry("Park Place", "parkplace.jpg"),
            Map.entry("Pennsylvania Avenue", "pennsylvaniaavenue.jpg"),
            Map.entry("Pennsylvania Railroad", "pennsylvaniarailroad.jpg"),
            Map.entry("Property Wild Card", "propertywildcard.jpg"),
            Map.entry("Reading Railroad", "readingrailroad.jpg"),
            Map.entry("Short Line", "shortline.jpg"),
            Map.entry("States Avenue", "statesavenue.jpg"),
            Map.entry("St. Charles Place", "stcharlesplace.jpg"),
            Map.entry("St. James Place", "stjamesplace.jpg"),
            Map.entry("Tennessee Avenue", "tennesseeavenue.jpg"),
            Map.entry("Ventnor Avenue", "ventnoravenue.jpg"),
            Map.entry("Vermont Avenue", "vermontavenue.jpg"),
            Map.entry("Virginia Avenue", "virginiaavenue.jpg"),
            Map.entry("Water Works", "waterworks.jpg"),
            Map.entry("Wild Blue-Green", "wildbg.jpg"),
            Map.entry("Wild Railroad-Green", "wildgt.jpg"),
            Map.entry("Wild Light Blue-Brown", "wildlbbr.jpg"),
            Map.entry("Wild Railroad-Light Blue", "wildlbt.jpg"),
            Map.entry("Wild Orange-Pink", "wildop.jpg"),
            Map.entry("Wild Yellow-Red", "wildry.jpg"),
            Map.entry("Wild Utility-Railroad", "wildut.jpg"),
            Map.entry("Card Back", "cardback.jpg")
    );
    private static final Map<String, String> CARD_NAME_ALIAS = Map.ofEntries(
            Map.entry("1M Money", "One Million"),
            Map.entry("2M Money", "Two Million"),
            Map.entry("3M Money", "Three Million"),
            Map.entry("4M Money", "Four Million"),
            Map.entry("5M Money", "Five Million"),
            Map.entry("10M Money", "Ten Million"),
            Map.entry("Brown Rent", "Rent Light Blue-Brown"),
            Map.entry("Light Blue Rent", "Rent Light Blue-Brown"),
            Map.entry("Pink Rent", "Rent Orange-Pink"),
            Map.entry("Orange Rent", "Rent Orange-Pink"),
            Map.entry("Red Rent", "Rent Yellow-Red"),
            Map.entry("Yellow Rent", "Rent Yellow-Red"),
            Map.entry("Green Rent", "Rent Blue-Green"),
            Map.entry("Deep Blue Rent", "Rent Blue-Green"),
            Map.entry("Railroad Rent", "Rent Utility-Railroad"),
            Map.entry("Utility Rent", "Rent Utility-Railroad"),
            Map.entry("Light Blue/Brown Rent", "Rent Light Blue-Brown"),
            Map.entry("Railroad/Utility Rent", "Rent Utility-Railroad"),
            Map.entry("B&O Railroad", "B & O Railroad"),
            Map.entry("Wild Property", "Property Wild Card"),
            Map.entry("Light Blue/Brown Multi", "Wild Light Blue-Brown"),
            Map.entry("Light Blue/Railroad Multi", "Wild Railroad-Light Blue"),
            Map.entry("Pink/Orange Multi", "Wild Orange-Pink"),
            Map.entry("Red/Yellow Multi", "Wild Yellow-Red"),
            Map.entry("Deep Blue/Green Multi", "Wild Blue-Green"),
            Map.entry("Green/Railroad Multi", "Wild Railroad-Green"),
            Map.entry("Railroad/Utility Multi", "Wild Utility-Railroad")
    );
    private static final Map<String, Image> CARD_IMAGE_CACHE = new HashMap<>();

    private CardImageRegistry() {
    }

    public static ImageView buildCardImageView(Card card) {
        String resourcePath = resolveCardImageResource(card.name());
        if (resourcePath == null) {
            return null;
        }
        Image image = loadImage(resourcePath);
        if (image == null) {
            return null;
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(160);
        imageView.setFitHeight(228);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }

    public static Image loadImage(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }
        Image image = CARD_IMAGE_CACHE.get(resourcePath);
        if (image != null) {
            return image;
        }
        try (var stream = CardImageRegistry.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            image = new Image(stream);
            if (image.isError()) {
                return null;
            }
            CARD_IMAGE_CACHE.put(resourcePath, image);
            return image;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String cardBackResource() {
        return CARD_IMG_BASE + "cardback new.jpg";
    }

    private static String resolveCardImageResource(String cardName) {
        String canonical = CARD_NAME_ALIAS.getOrDefault(cardName, cardName);
        String fileName = CARD_NAME_TO_FILE.get(canonical);
        if (fileName == null) {
            return null;
        }
        String stem = fileName.replaceFirst("\\.jpg$", "");
        String newName = stem + " new.jpg";
        String resourcePath = CARD_IMG_BASE + newName;
        return CardImageRegistry.class.getResource(resourcePath) == null ? null : resourcePath;
    }
}
