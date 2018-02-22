package net.reini.swissfxknife;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Controller {
    private static final byte[] JAAS_KB =
            {'j', 'a', 'a', 's', ' ', 'i', 's', ' ', 't', 'h', 'e', ' ', 'w', 'a', 'y'};
    private static final byte[] INSTALLER_KB = {76, -101, -45, 56, 119, 73, -40, 112, -91, 3, -11,
            55, -38, -43, -122, -92, 76, -101, -45, 56, 119, 73, -40, 112};
    private static final byte[] DBEAVER_ENCRYPTION_KEY =
            "sdf@!#$verf^wv%6Fwe%$$#FFGwfsdefwfe135s$^H)dg".getBytes();
    private static final SecretKeySpec JAAS_KEYSPEC = new SecretKeySpec(JAAS_KB, "Blowfish");
    private static final SecretKeySpec INSTALLER_KEYSPEC =
            new SecretKeySpec(INSTALLER_KB, "TripleDES");
    private static final Pattern INSTALLER_KEY_PATTERN = Pattern.compile("^_ENC_(.*)_ENC_$");
    private static final KeyCodeCombination OPEN_FILE_COMBINATION =
            new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN);

    private final ObservableList<ConfigEntry> configData = FXCollections.observableArrayList();

    private Cipher jaasCipher;
    private Cipher installerCipher;
    private Stage mainStage;
    private Preferences prefs;

    @FXML
    private TextField jaasPassword;
    @FXML
    private TextField jaasPasswordEncrypted;
    @FXML
    private Label jaasErrorReason;

    @FXML
    private TextField dbeaverPassword;
    @FXML
    private TextField dbeaverPasswordEncrypted;
    @FXML
    private Label dbeaverErrorReason;

    @FXML
    private TextField installerPassword;
    @FXML
    private TextField installerPasswordEncrypted;
    @FXML
    private Label installerErrorReason;

    @FXML
    private TextField configFile;
    @FXML
    private TableView<ConfigEntry> configContent;
    @FXML
    private Button addConfig;
    @FXML
    private Button loadConfig;
    @FXML
    private Button saveConfig;

    @FXML
    private void initialize() throws Exception {
        jaasCipher = Cipher.getInstance("Blowfish");
        jaasPassword.textProperty().addListener(this::jaasPasswordChanged);
        jaasPasswordEncrypted.textProperty().addListener(this::jaasPasswordEncryptedChanged);
        dbeaverPassword.textProperty().addListener(this::dbeaverPasswordChanged);
        dbeaverPasswordEncrypted.textProperty().addListener(this::dbeaverPasswordEncryptedChanged);
        installerCipher = Cipher.getInstance("TripleDES");
        installerPassword.textProperty().addListener(this::installerPasswordChanged);
        installerPasswordEncrypted.textProperty()
                .addListener(this::installerPasswordEncryptedChanged);
        TableColumn<ConfigEntry, String> key = new TableColumn<>("Key");
        key.setSortable(false);
        key.setMinWidth(150.0);
        key.setPrefWidth(200.0);
        key.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("key"));
        TableColumn<ConfigEntry, String> value = new TableColumn<>("Value");
        value.setSortable(false);
        value.setMinWidth(200.0);
        value.setPrefWidth(500.0);
        value.setCellValueFactory(new PropertyValueFactory<ConfigEntry, String>("value"));
        value.setCellFactory(TextFieldTableCell.forTableColumn());
        ObservableList<TableColumn<ConfigEntry, ?>> columns = configContent.getColumns();
        columns.add(key);
        columns.add(value);
        configContent.setEditable(true);
        configContent.itemsProperty().set(configData.sorted(
                Comparator.comparing(c -> c.getKey().toLowerCase(), Comparator.naturalOrder())));
        configFile.textProperty().addListener(this::configFileChanged);
        configFile.setOnDragOver(event -> {
            /*
             * accept it only if it is not dragged from the same node and if it has a string data
             */
            if (event.getGestureSource() != configFile && event.getDragboard().hasFiles()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.ANY);
            }
            event.consume();
        });
        configFile.setOnDragDropped(event -> {
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
            /*
             * let the source know whether the string was successfully transferred and used
             */
            event.setDropCompleted(success);
            event.consume();
        });
        configFile.setOnKeyPressed(event -> {
            if (OPEN_FILE_COMBINATION.match(event)) {
                Platform.runLater(() -> selectFile());
                event.consume();
            }
        });
        addConfig.disableProperty().bind(configFile.textProperty().isEmpty());
        addConfig.setOnAction(event -> {
            configData.add(ConfigEntry.create("", ""));
        });
        loadConfig.disableProperty().bind(configFile.textProperty().isEmpty());
        loadConfig.setOnAction(event -> loadConfig(configFile.getText()));
        saveConfig.disableProperty().bind(configFile.textProperty().isEmpty());
        saveConfig.setOnAction(event -> saveConfig(configFile.getText()));
    }

    void initialize(Stage stage, Preferences preferences) {
        mainStage = stage;
        prefs = preferences;
    }

    void jaasPasswordChanged(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
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

    void jaasPasswordEncryptedChanged(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
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

    void dbeaverPasswordChanged(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
        byte[] e = newValue.getBytes(StandardCharsets.UTF_8);
        byte[] plainBytes = Arrays.copyOf(e, e.length + 2);
        plainBytes[plainBytes.length - 2] = 0;
        plainBytes[plainBytes.length - 1] = -127;
        xorStringByKey(plainBytes);
        dbeaverPasswordEncrypted.setText(Base64.getEncoder().encodeToString(plainBytes));
        dbeaverErrorReason.setText("");
    }

    void dbeaverPasswordEncryptedChanged(ObservableValue<? extends String> observable,
            String oldValue, String newValue) {
        if (newValue != null && !newValue.isEmpty()) {
            byte[] e = Base64.getDecoder().decode(newValue);
            xorStringByKey(e);
            if (e[e.length - 2] == 0 && e[e.length - 1] == -127) {
                dbeaverPassword.setText(new String(e, 0, e.length - 2, StandardCharsets.UTF_8));
                dbeaverErrorReason.setText("");
            } else {
                dbeaverErrorReason.setText("Invalid encrypted string");
            }
        } else {
            dbeaverPassword.setText("");
            dbeaverErrorReason.setText("");
        }
    }

    private static void xorStringByKey(byte[] plainBytes) {
        int keyOffset = 0;
        for (int i = 0; i < plainBytes.length; ++i) {
            byte keyChar = DBEAVER_ENCRYPTION_KEY[keyOffset];
            ++keyOffset;
            if (keyOffset >= DBEAVER_ENCRYPTION_KEY.length) {
                keyOffset = 0;
            }
            plainBytes[i] ^= keyChar;
        }
    }

    void installerPasswordChanged(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
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

    void installerPasswordEncryptedChanged(ObservableValue<? extends String> observable,
            String oldValue, String newValue) {
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

    void configFileChanged(ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
        if (newValue.equals(oldValue)) {
            return;
        } else if (newValue.isEmpty()) {
            configData.clear();
        }
        loadConfig(newValue);
    }

    void loadConfig(String filename) {
        Platform.runLater(() -> {
            configData.clear();
            ConfigAccess.read(Paths.get(filename),
                    (k, v) -> configData.add(ConfigEntry.create(k, v)));
        });
    }

    void saveConfig(String filename) {
        Platform.runLater(() -> ConfigAccess.write(Paths.get(filename), valueConsumer -> configData
                .forEach(entry -> valueConsumer.accept(entry.getKey(), entry.getValue()))));
    }

    void selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select config file");
        fileChooser.setInitialDirectory(
                new File(prefs.get("configFileDir", System.getProperty("user.home"))));
        fileChooser.getExtensionFilters()
                .add(new ExtensionFilter("Config files", "config.jar", "config.bin"));
        File file = fileChooser.showOpenDialog(mainStage);
        if (file != null) {
            prefs.put("configFileDir", file.getParentFile().getAbsolutePath());
            configFile.setText(file.getAbsolutePath());
        }
    }
}
