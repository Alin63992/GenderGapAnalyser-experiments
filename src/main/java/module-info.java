module com.gendergapanalyser.gendergapanalyser {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires commons.math3;
    requires org.apache.commons.io;
    requires commons.email;
    requires commons.validator;
    requires javafx.base;
    requires javafx.graphics;
    requires itextpdf;
    requires org.controlsfx.controls;
    requires AnimateFX;
    requires eu.iamgio.animated;


    opens com.gendergapanalyser.gendergapanalyser to javafx.fxml;
    exports com.gendergapanalyser.gendergapanalyser;
}