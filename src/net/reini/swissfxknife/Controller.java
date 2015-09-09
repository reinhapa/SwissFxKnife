package net.reini.swissfxknife;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

public class Controller {
	private static final byte[] JAAS_KB = { 'j', 'a', 'a', 's', ' ', 'i', 's',
			' ', 't', 'h', 'e', ' ', 'w', 'a', 'y' };
	private static final byte[] INSTALLER_KB = { -65, -51, 99, 35, 103, -62,
			-95, -118, -83, 91, -102, -113, 89, -108, -44, 57, -65, -51, 99,
			35, 103, -62, -95, -118 };

	private static final SecretKeySpec JAAS_KEYSPEC = new SecretKeySpec(
			JAAS_KB, "Blowfish");
	private static final SecretKeySpec INSTALLER_KEYSPEC = new SecretKeySpec(
			INSTALLER_KB, "TripleDES");

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
	private void initialize() throws Exception {
		jaasCipher = Cipher.getInstance("Blowfish");
		jaasPassword.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					try {
						jaasCipher.init(Cipher.ENCRYPT_MODE, JAAS_KEYSPEC);
						byte[] encoding = jaasCipher.doFinal(newValue
								.getBytes(StandardCharsets.UTF_8));
						String encoded = new BigInteger(encoding).toString(16);
						jaasPasswordEncrypted.setText(encoded);
						jaasErrorReason.setText("");
					} catch (InvalidKeyException | IllegalBlockSizeException
							| BadPaddingException e) {
						jaasErrorReason.setText(e.toString());
					}
				});
		jaasPasswordEncrypted.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					try {
						byte[] encoding = new BigInteger(newValue, 16)
								.toByteArray();
						jaasCipher.init(Cipher.DECRYPT_MODE, JAAS_KEYSPEC);
						byte[] decode = jaasCipher.doFinal(encoding);
						jaasPassword.setText(new String(decode,
								StandardCharsets.UTF_8));
						jaasErrorReason.setText("");
					} catch (NumberFormatException e) {
						jaasErrorReason.setText("Format wrong: "
								+ e.getMessage());
					} catch (InvalidKeyException | IllegalBlockSizeException
							| BadPaddingException e) {
						jaasErrorReason.setText("Decode failed: "
								+ e.getMessage());
					}
				});

		installerCipher = Cipher.getInstance("TripleDES");
		installerPassword.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					try {
						installerCipher.init(Cipher.ENCRYPT_MODE,
								INSTALLER_KEYSPEC);
						byte[] encoding = installerCipher.doFinal(newValue
								.getBytes(StandardCharsets.UTF_8));
						String encoded = new BigInteger(encoding).toString(16);
						installerPasswordEncrypted.setText(encoded);
						installerErrorReason.setText("");
					} catch (InvalidKeyException | IllegalBlockSizeException
							| BadPaddingException e) {
						installerErrorReason.setText(e.toString());
					}
				});
		installerPasswordEncrypted.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					try {
						byte[] encoding = new BigInteger(newValue, 16)
								.toByteArray();
						installerCipher.init(Cipher.DECRYPT_MODE,
								INSTALLER_KEYSPEC);
						byte[] decode = installerCipher.doFinal(encoding);
						installerPassword.setText(new String(decode,
								StandardCharsets.UTF_8));
						installerErrorReason.setText("");
					} catch (NumberFormatException e) {
						installerErrorReason.setText("Format wrong: "
								+ e.getMessage());
					} catch (InvalidKeyException | IllegalBlockSizeException
							| BadPaddingException e) {
						installerErrorReason.setText("Decode failed: "
								+ e.getMessage());
					}
				});

	}

	@FXML
	public void encrypt() {
	}

	@FXML
	public void dencrypt() {
	}
}