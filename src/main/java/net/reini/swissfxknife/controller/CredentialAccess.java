/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.reini.swissfxknife.controller;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.util.Collections.list;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.spec.SecretKeySpec;

/**
 * Central class to read and write bison elytron credential key store.
 *
 * @author Patrick Reinhart
 */
final class CredentialAccess {
    private static final char[] KEYSTORE_PASSWORD = "3lytr0n".toCharArray();
    private static final Logger LOGGER = Logger.getLogger(ConfigAccess.class.getName());

    private CredentialAccess() {
    }

    static void read(Path config, BiConsumer<String, String> valueConsumer) {
        if (isRegularFile(config)) {
            try {
                KeyStore keyStore = loadKeystore(config);
                for (String alias : list(keyStore.aliases())) {
                    Key key = keyStore.getKey(alias, KEYSTORE_PASSWORD);
                    valueConsumer.accept(alias, getDecoded(key.getEncoded()));
                }
            } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException
                    | IOException | UnrecoverableKeyException e) {
                LOGGER.log(Level.SEVERE, "Failed to read: " + config, e);
            }
        }
    }

    static void write(Path config, Consumer<BiConsumer<String, String>> valueConsumer) {
        if (isRegularFile(config)) {
            try {
                KeyStore keyStore = loadKeystore(config);
                Set<String> currentCredentials = new HashSet<>(list(keyStore.aliases()));
                valueConsumer.accept(
                        (key, value) -> updateKey(keyStore, currentCredentials, key, value));
                for (String alias : currentCredentials) {
                    keyStore.deleteEntry(alias);
                }
                try (OutputStream out = newOutputStream(config)) {
                    keyStore.store(out, KEYSTORE_PASSWORD);
                }
            } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException
                    | IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to write: " + config, e);
            }
        }
    }

    static void updateKey(KeyStore keyStore, Set<String> currentCredentials, String alias,
                          String clearPassword) {
        try {
            currentCredentials.remove(alias);
            Key key = new SecretKeySpec(getEncoded(clearPassword), "1.2.840.113549.1.7.1");
            keyStore.setKeyEntry(alias, key, KEYSTORE_PASSWORD, null);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Unable to update key", e);
        }
    }


    static KeyStore loadKeystore(Path config)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance("jceks");
        try (InputStream in = newInputStream(config)) {
            keyStore.load(in, KEYSTORE_PASSWORD);
        }
        return keyStore;
    }

    static byte[] getEncoded(String clearText) {
        byte[] utf8bytes = clearText.getBytes(StandardCharsets.UTF_8);
        int length = utf8bytes.length;
        byte[] result = new byte[length + 2];
        result[0] = 4;
        result[1] = (byte) length;
        System.arraycopy(utf8bytes, 0, result, 2, length);
        return result;
    }

    static String getDecoded(byte[] encoded) {
        if (encoded.length > 1) {
            if (encoded[0] == 4) {
                return new String(encoded, 2, encoded[1], StandardCharsets.UTF_8);
            } else {
                LOGGER.severe(() -> "Encoded type wrong: " + encoded[0]);
            }
        }else {
            LOGGER.severe(() -> "Encoded length too small: " + encoded.length);
        }
        return "";
    }
}
