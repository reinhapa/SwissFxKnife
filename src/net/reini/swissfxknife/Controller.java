package net.reini.swissfxknife;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final byte[] INSTALLER_KB = { 76, -101, -45, 56, 119, 73,
			-40, 112, -91, 3, -11, 55, -38, -43, -122, -92, 76, -101, -45, 56,
			119, 73, -40, 112 };

	private static final SecretKeySpec JAAS_KEYSPEC = new SecretKeySpec(
			JAAS_KB, "Blowfish");
	private static final SecretKeySpec INSTALLER_KEYSPEC = new SecretKeySpec(
			INSTALLER_KB, "TripleDES");;
	private static final Pattern INSTALLER_KEY_PATTERN = Pattern
			.compile("^_ENC_(.*)_ENC_$");

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
						String encoded = Base64.getEncoder().encodeToString(
								encoding);
						installerPasswordEncrypted.setText(String.format(
								"_ENC_%s_ENC_", encoded));
						installerErrorReason.setText("");
					} catch (InvalidKeyException | IllegalBlockSizeException
							| BadPaddingException e) {
						installerErrorReason.setText(e.toString());
					}
				});
		installerPasswordEncrypted.textProperty().addListener(
				(observable, oldValue, newValue) -> {
					try {
						Matcher matcher = INSTALLER_KEY_PATTERN
								.matcher(newValue);
						byte[] encoding;
						if (matcher.matches()) {
							encoding = Base64.getDecoder().decode(
									matcher.group(1));
						} else {
							encoding = Base64.getDecoder().decode(newValue);
						}
						installerCipher.init(Cipher.DECRYPT_MODE,
								INSTALLER_KEYSPEC);
						byte[] decode = installerCipher.doFinal(encoding);
						installerPassword.setText(new String(decode,
								StandardCharsets.UTF_8));
						installerErrorReason.setText("");
					} catch (IllegalArgumentException e) {
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