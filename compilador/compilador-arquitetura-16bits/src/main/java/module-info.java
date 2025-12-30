module com.unipampa.compiladorarquitetura16bits {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.unipampa.compiladorarquitetura16bits to javafx.fxml;
    exports com.unipampa.compiladorarquitetura16bits;
}