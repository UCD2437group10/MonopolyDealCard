module MonopolyDealCard.main {
    requires javafx.controls;
    requires javafx.fxml;

    exports edu.group10.FrontEnd;
    opens edu.group10.FrontEnd to javafx.fxml;
}