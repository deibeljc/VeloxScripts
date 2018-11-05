import javafx.fxml.FXML;
import org.dreambot.api.javafx.JavaFXTools;

public class Dialog extends org.dreambot.api.javafx.Dialog {

    public Dialog() {
        JavaFXTools.load(getClass().getResource("Main.fxml"), this);
    }

    @Override
    public String getTitle() {
        return "Velox Combat";
    }

    @FXML
    public void onClose() {
        getScene().getWindow().hide();
    }

    /**
     * Whatever actions you would like to preform when
     * the dialog is minimized.
     */
    @FXML
    void onMinimize() {
        getStage().setIconified(true);
    }
}
