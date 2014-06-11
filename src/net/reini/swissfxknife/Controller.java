package net.reini.swissfxknife;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

public class Controller {
	private static final byte[] KB = { 'j', 'a', 'a', 's', ' ', 'i', 's', ' ',
			't', 'h', 'e', ' ', 'w', 'a', 'y' };
	private static final SecretKeySpec KEYSPEC = new SecretKeySpec(KB,
			"Blowfish");
	private Cipher cipher;

	@FXML
	private TextField password;
	@FXML
	private TextField passwordEncrypted;
	@FXML
	private Label errorReason;

	@FXML
	private void initialize() throws Exception {
		cipher = Cipher.getInstance("Blowfish");
	}

	@FXML
	public void encrypt() {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, KEYSPEC);
			byte[] encoding = cipher.doFinal(password.textProperty()
					.getValueSafe().getBytes(Charset.forName("UTF-8")));
			StringBuilder encoded = new StringBuilder(encoding.length * 2);
			for (byte element : encoding) {
				String byteStr = Integer.toHexString(element & 255);
				if (byteStr.length() == 1) {
					encoded.append('0');
				}
				encoded.append(byteStr);
			}
			passwordEncrypted.setText(encoded.toString());
			errorReason.setText("");
		} catch (InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			errorReason.setText(e.toString());
		}
	}

	@FXML
	public void dencrypt() {
		try {
			String secret = passwordEncrypted.textProperty().getValueSafe();
			byte[] encoding = new byte[secret.length() / 2];
			for (int i = 0, si = 0, n = encoding.length; i < n; i++, si += 2) {
				String hexString = secret.substring(si, si + 2);
				encoding[i] = (byte) Integer.parseInt(hexString, 16);
			}
			cipher.init(Cipher.DECRYPT_MODE, KEYSPEC);
			byte[] decode = cipher.doFinal(encoding);
			password.setText(new String(decode, Charset.forName("UTF-8")));
			errorReason.setText("");
		} catch (NumberFormatException | InvalidKeyException
				| IllegalBlockSizeException | BadPaddingException e) {
			errorReason.setText(e.toString());
		}
	}
}
