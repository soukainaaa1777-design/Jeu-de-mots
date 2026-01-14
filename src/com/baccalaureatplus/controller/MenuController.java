package com.baccalaureatplus.controller;

import javafx.animation.ScaleTransition;
import javafx.animation.Interpolator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class MenuController {

    @FXML private StackPane logoContainer;
    @FXML private Button btnSolo;
    @FXML private Button btnMulti;

    @FXML
    public void initialize() {
        // Animation de pulsation du logo (respiration douce)
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.5), logoContainer);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.06);
        pulse.setToY(1.06);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.play();

        // Animation des boutons au survol
        configurerAnimationBouton(btnSolo);
        configurerAnimationBouton(btnMulti);
    }

    private void configurerAnimationBouton(Button btn) {
        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.08);
            st.setToY(1.08);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    @FXML
    private void lancerSolo(ActionEvent event) throws IOException {
        changerScene(event, "/com/baccalaureatplus/app/solo.fxml");
    }

    @FXML
    private void lancerMulti(ActionEvent event) throws IOException {
        changerScene(event, "/com/baccalaureatplus/app/multiplayer.fxml");
    }

    private void changerScene(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }
}