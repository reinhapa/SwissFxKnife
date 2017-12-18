package net.reini.swissfxknife;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class Controller {
	private static final byte[] JAAS_KB = { 'j', 'a', 'a', 's', ' ', 'i', 's', ' ', 't', 'h', 'e', ' ', 'w', 'a', 'y' };
    private static final byte[] INSTALLER_KB = { 76, -101, -45, 56, 119, 73, -40, 112, -91, 3, -11, 55, -38, -43, -122,
            -92, 76, -101, -45, 56, 119, 73, -40, 112 };

    private static final SecretKeySpec JAAS_KEYSPEC = new SecretKeySpec(JAAS_KB, "Blowfish");
    private static final SecretKeySpec INSTALLER_KEYSPEC = new SecretKeySpec(INSTALLER_KB, "TripleDES");
    private static final Pattern INSTALLER_KEY_PATTERN = Pattern.compile("^_ENC_(.*)_ENC_$");

    private Cipher jaasCipher;
    private Cipher installerCipher;

    @FXML
    private TextField jaasPassword;
    @FXML
    private TextField jaasPasswordEncrypted;
    @FXML
    private Label jaasErrorReason;

    @FXML
    private TextField installerPassword;
    @FXML
    private TextField installerPasswordEncrypted;
    @FXML
    private Label installerErrorReason;

    @FXML
    private TextField configFile;
    @FXML
    private TableView<Entry<String, String>> configContent;
    @FXML
    private Button loadConfig;
    @FXML
    private Button saveConfig;

    @FXML
    private void initialize() throws Exception {
        jaasCipher = Cipher.getInstance("Blowfish");
        jaasPassword.textProperty().addListener(this::jaasPasswordChanged);
        jaasPasswordEncrypted.textProperty().addListener(this::jaasPasswordEncryptedChanged);
        installerCipher = Cipher.getInstance("TripleDES");
        installerPassword.textProperty().addListener(this::installerPasswordChanged);
        installerPasswordEncrypted.textProperty().addListener(this::installerPasswordEncryptedChanged);
        configFile.textProperty().addListener(this::configFileChanged);
        configFile.setOnDragOver(new EventHandler <DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                /* accept it only if it is  not dragged from the same node 
                 * and if it has a string data */
                if (event.getGestureSource() != configFile &&
                        event.getDragboard().hasFiles()) {
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.ANY);
                }
                event.consume();
            }
        });
        configFile.setOnDragDropped(new EventHandler <DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    List<File> files = db.getFiles();
                    if (!files.isEmpty()) {
                        configFile.setText(files.get(0).getAbsolutePath());
                        success = true;
                    }
                }
                /* let the source know whether the string was successfully 
                 * transferred and used */
                event.setDropCompleted(success);
                event.consume();
            }
        });
        loadConfig.disableProperty().bind(configFile.textProperty().isEmpty());
        saveConfig.disableProperty().bind(configFile.textProperty().isEmpty());
    }

    @FXML
    public void encrypt() {
    }

    @FXML
    public void dencrypt() {
    }

    void jaasPasswordChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        try {
            jaasCipher.init(Cipher.ENCRYPT_MODE, JAAS_KEYSPEC);
            byte[] encoding = jaasCipher.doFinal(newValue.getBytes(StandardCharsets.UTF_8));
            String encoded = new BigInteger(encoding).toString(16);
            jaasPasswordEncrypted.setText(encoded);
            jaasErrorReason.setText("");
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            jaasErrorReason.setText(e.toString());
        }
    }

    void jaasPasswordEncryptedChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        try {
            byte[] encoding = new BigInteger(newValue, 16).toByteArray();
            jaasCipher.init(Cipher.DECRYPT_MODE, JAAS_KEYSPEC);
            byte[] decode = jaasCipher.doFinal(encoding);
            jaasPassword.setText(new String(decode, StandardCharsets.UTF_8));
            jaasErrorReason.setText("");
        } catch (NumberFormatException e) {
            jaasErrorReason.setText("Format wrong: " + e.getMessage());
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            jaasErrorReason.setText("Decode failed: " + e.getMessage());
        }
    }

    void installerPasswordChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        try {
            installerCipher.init(Cipher.ENCRYPT_MODE, INSTALLER_KEYSPEC);
            byte[] encoding = installerCipher.doFinal(newValue.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(encoding);
            installerPasswordEncrypted.setText(String.format("_ENC_%s_ENC_", encoded));
            installerErrorReason.setText("");
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            installerErrorReason.setText(e.toString());
        }
    }

    void installerPasswordEncryptedChanged(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
        try {
            Matcher matcher = INSTALLER_KEY_PATTERN.matcher(newValue);
            byte[] encoding;
            if (matcher.matches()) {
                encoding = Base64.getDecoder().decode(matcher.group(1));
            } else {
                encoding = Base64.getDecoder().decode(newValue);
            }
            installerCipher.init(Cipher.DECRYPT_MODE, INSTALLER_KEYSPEC);
            byte[] decode = installerCipher.doFinal(encoding);
            installerPassword.setText(new String(decode, StandardCharsets.UTF_8));
            installerErrorReason.setText("");
        } catch (IllegalArgumentException e) {
            installerErrorReason.setText("Format wrong: " + e.getMessage());
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            installerErrorReason.setText("Decode failed: " + e.getMessage());
        }
    }

    void configFileChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        System.out.println(observable + " "+ oldValue +" "+newValue);
    }
}