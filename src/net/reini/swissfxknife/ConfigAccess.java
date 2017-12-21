package net.reini.swissfxknife;


import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * Central class to encode and decode configuration specific informations for bison
 * 
 * @author Patrick Reinhart
 */
final class ConfigAccess {
    private static final String C_TYPE = "DES";
    private static final String CONFIG_BIN_FILENAME = "config.bin";

    private ConfigAccess() {
    }

    static void read(Path config, BiConsumer<String, String> valueConsumer) {
        if (isRegularFile(config)) {
            try (InputStream in = newInputStream(config)) {
                if (config.endsWith(Paths.get(CONFIG_BIN_FILENAME))) {
                    readConfig(in, valueConsumer);
                } else {
                    try (ZipInputStream zipIn = new ZipInputStream(in)) {
                        ZipEntry entry = null;
                        while ((entry = zipIn.getNextEntry()) != null) {
                            if (CONFIG_BIN_FILENAME.equals(entry.getName())) {
                                readConfig(zipIn, valueConsumer);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void write(Path config, Consumer<BiConsumer<String, String>> valueConsumer) {
        if (isRegularFile(config)) {
            try (OutputStream out = newOutputStream(config)) {
                if (config.endsWith(Paths.get(CONFIG_BIN_FILENAME))) {
                    writeConfig(out, valueConsumer);
                } else {
                    try (JarOutputStream jarOut = new JarOutputStream(out)) {
                        // now writing the content of the byte array output stream to a jar entry
                        jarOut.putNextEntry(new JarEntry(CONFIG_BIN_FILENAME));
                        writeConfig(new IgnoreCloseOutputStream(jarOut), valueConsumer);
                        jarOut.finish();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void readConfig(InputStream in, BiConsumer<String, String> valueConsumer)
            throws IOException, GeneralSecurityException, ClassNotFoundException {
        byte[] key = new byte[8];
        if (in.read(key) != 8) {
            throw new IllegalStateException("Unable to read key");
        }
        Cipher cipher = Cipher.getInstance(C_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, C_TYPE));
        try (ObjectInputStream objIn = new ObjectInputStream(
                new GZIPInputStream(new CipherInputStream(new BufferedInputStream(in), cipher)))) {
            Properties props = (Properties) objIn.readObject();
            props.forEach((k, v) -> valueConsumer.accept(String.valueOf(k), String.valueOf(v)));
        }
    }

    private static void writeConfig(OutputStream out,
            Consumer<BiConsumer<String, String>> valueConsumer)
            throws IOException, GeneralSecurityException {
        SecretKey secretKey = KeyGenerator.getInstance(C_TYPE).generateKey();
        out.write(secretKey.getEncoded());
        Cipher cipher = Cipher.getInstance(C_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        try (ObjectOutputStream objOut = new ObjectOutputStream(new GZIPOutputStream(
                new CipherOutputStream(new BufferedOutputStream(out), cipher)))) {
            Properties properties = new Properties();
            valueConsumer.accept(properties::setProperty);
            objOut.writeObject(properties);
            objOut.flush();
        }
    }
}
