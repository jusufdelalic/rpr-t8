package ba.unsa.etf.rpr.tutorijal8;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

public class Controller {
    public Button trazi;
    public TextField pretraga;// kriterij pretrage
    public ListView<String> listaPutanja; // ListView Stringova, odnosno
                                          // apsolutnih putanja trazenih datoteka
    public Button prekid;

    @FXML
    public void initialize() {
        prekid.setDisable(true); // na pocetku nemamo nista prekidati...
        trazi.disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                prekid.setDisable(true);
            else
                prekid.setDisable(false);
        });

        listaPutanja.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            //changed - da li se vrijednost ObservableValue promijenila
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Parent root = null;
                try {
                    root = FXMLLoader.load(getClass().getResource("slanje.fxml"));
                } catch (IOException ignore) {
                    return;
                }
                Stage pomocniProzor = new Stage();
                pomocniProzor.setTitle("Unos podataka");
                pomocniProzor.resizableProperty().setValue(false); // nemogućnost mijenjanja velicine prozora
                pomocniProzor.setScene(new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE));
                pomocniProzor.initModality(Modality.APPLICATION_MODAL);
                pomocniProzor.show();
            }
        });
    }


    // atribut-klasa Pretraga
    // Runnable - an interface that is to be implemented by a class whose instances are intended to be executed by a thread
    private class Pretraga implements Runnable { //

        private File korijen;

        public Pretraga(String home) {
            this.korijen = new File(home);
        }

        // implementacija run iz interfejsa runable
        @Override
        public void run() {

            pretrazi(korijen, korijen); // prilikom pokretanja krećemo od korisničkog home foldera - korijen, trenutni je na
                                        // pocetku korijen
        }

        public void pretrazi(File korijen, File trenutni) {
            if (!trazi.isDisabled()) // ako je dugme za pretragu omogućeno...
                Thread.currentThread().stop(); // gotova je pretraga...
            if (trenutni.isDirectory()) { // "trenutni" je direktorij
                File[] listFiles = trenutni.listFiles(); // lista datoteka u tom folderu "trenutni"
                if (listFiles == null)
                    return; // nema datoteka u trenutnom direktoriju, prekidamo nit
                for (File file : listFiles) {
                    if (file.isDirectory()) { // ako je file direktorij, tj. u trenutnom ima jos direktorija
                        pretrazi(korijen, file); // rekurzivno zovemo pocevsi od tog novog foldera...
                    }
                    if (file.isFile()) {
                        if (file.getName().contains(pretraga.getText())) // ako ima substring tekst pretrage...
                            // dodajemo na listu putanja
                            Platform.runLater(()->listaPutanja.getItems().add(file.getAbsolutePath()));
                        // Platform - klasa za podrsku (visenitnim) aplikacijama
                        // metoda runLater - run the specified Runnable on the JavaFX Application Thread at some unspecified time in the future.
                    }
                }
            }
            if (korijen.getAbsolutePath().equals(trenutni.getAbsolutePath()))
                trazi.setDisable(false);
        }
    }

    public void traziClick(ActionEvent actionEvent) {
        trazi.setDisable(true);

        // Returns the currently installed selection model.
        // prilikom nove pretrage brise se stara...
        listaPutanja.getSelectionModel().clearSelection();
        listaPutanja.getItems().clear();

        String home = System.getProperty("user.home"); // pocetni korisnikov folder
        Pretraga pretraga = new Pretraga(home);
        Thread thread = new Thread(pretraga);
        thread.start();
    }

    public void prekiniClick(ActionEvent actionEvent) {
        trazi.setDisable(false);
    }
}