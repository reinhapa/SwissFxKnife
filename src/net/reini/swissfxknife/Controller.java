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
	password.textProperty().addListener(
		(observable, oldValue, newValue) -> {
		    try {
			cipher.init(Cipher.ENCRYPT_MODE, KEYSPEC);
			byte[] encoding = cipher.doFinal(newValue
				.getBytes(StandardCharsets.UTF_8));
			String encoded = new BigInteger(encoding).toString(16);
			passwordEncrypted.setText(encoded);
			errorReason.setText("");
		    } catch (InvalidKeyException | IllegalBlockSizeException
			    | BadPaddingException e) {
			errorReason.setText(e.toString());
		    }
		});
	passwordEncrypted.textProperty()
		.addListener(
			(observable, oldValue, newValue) -> {
			    try {
				byte[] encoding = new BigInteger(newValue, 16)
					.toByteArray();
				cipher.init(Cipher.DECRYPT_MODE, KEYSPEC);
				byte[] decode = cipher.doFinal(encoding);
				password.setText(new String(decode,
					StandardCharsets.UTF_8));
				errorReason.setText("");
			    } catch (NumberFormatException e) {
				errorReason.setText("Format wrong: "
					+ e.getMessage());
			    } catch (InvalidKeyException
				    | IllegalBlockSizeException
				    | BadPaddingException e) {
				errorReason.setText("Decode failed: "
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
