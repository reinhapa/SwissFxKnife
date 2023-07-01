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

import org.junit.jupiter.api.Test;

import static java.util.Collections.list;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.util.HashSet;

import javax.crypto.spec.SecretKeySpec;


class DerStringTest {
    private static final char[] KEYSTORE_PASSWORD = "3lytr0n".toCharArray();

    @Test
    void testDEROctetString() throws Exception {
        byte[] derOctetBytes = CredentialAccess.getEncoded("clearPassword");
        System.out.println(derOctetBytes);

        KeyStore keyStore = KeyStore.getInstance("jceks");
        keyStore.load(null, KEYSTORE_PASSWORD);
        keyStore.setKeyEntry("chdml",
                new SecretKeySpec(getEncoded("chDML"), "1.2.840.113549.1.7.1"), KEYSTORE_PASSWORD,
                null);
        keyStore.setKeyEntry("chddl",
                new SecretKeySpec(getEncoded("chDDL"), "1.2.840.113549.1.7.1"), KEYSTORE_PASSWORD,
                null);

        for (String alias : new HashSet<>(list(keyStore.aliases()))) {
            Key key = keyStore.getKey(alias, KEYSTORE_PASSWORD);
            System.out.println(getDecoded(key.getEncoded()));
        }

        // try (OutputStream out = newOutputStream(Paths.get("/home/pr/test.keystore"))) {
        // keyStore.store(out, KEYSTORE_PASSWORD);
        // }
    }

    static byte[] getEncoded(String clearText) {
        byte[] utf8bytes = clearText.getBytes(StandardCharsets.UTF_8);
        int length = utf8bytes.length;
        byte[] result = new byte[length + 2];
        result[0] = 4;
        result[1] = (byte) length;
        System.arraycopy(utf8bytes, 0, result, 2, length);
        System.out.println(result);
        return result;
    }

    static String getDecoded(byte[] encoded) {
        if (encoded.length > 1 && encoded[0] == 4) {
            return new String(encoded, 2, encoded[1], StandardCharsets.UTF_8);
        }
        return "";
    }
}
