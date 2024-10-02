/*
 * JBoss, Home of Professional Open Source
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.fips;


import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

/**
 * BCFIPS demo for Keycloak db-password supply in the FIPS mode.
 *
 * @author <a href="mailto:pskopek@redhat.com">Peter Skopek</a>
 */
public class BCImportPass {


    private static final String keystorePassword = "secretPwd1";
    private static final String entryAlias = "kc.db-password";
    private static final String dbPassword = "secretDBPwd2";
    private static final String keyStoreType = "BCFKS";
    private static final String bouncyCastleProviderName = "BCFIPS";
    private static final String keyStoreFileName = "keystore.bcfks";


    static String[] keytoolImportPassArgs = new String[]{ // not working, just for debugging
        "-importpass",
        "-alias", entryAlias,
        "-keystore", keyStoreFileName,
        "-storepass", keystorePassword,
        "-storetype", keyStoreType,
        "-providername", bouncyCastleProviderName,
        "-providerclass", "org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider",
        "-provider", "org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider",
        "-providerpath", "./bc-fips-1.0.2.5.jar",
        //"-J-Djava.security.properties=/home/pskopek/dev/fips/java.security",
        "-keyalg", "PBKDF2withHmacSHA512",
        "-keypass", keystorePassword,
    };

    static String[] listArgs = new String[]{
            "-list",
            "-v",
            "-keystore", keyStoreFileName,
            "-storepass", keystorePassword,
            "-storetype", keyStoreType,
            "-providername", bouncyCastleProviderName,
            "-providerclass", "org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider",
            "-provider", "org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider",
            "-providerpath", "./bc-fips-1.0.2.5.jar",
    };

    public static void main(String[] args) throws Exception {

        // just for debugging
        //providerInspection();
        //keytool(keytoolImportPassArgs);

        keystoreDemo();
        keytool(listArgs);

    }

    /**
     * Simulates keytool command.
     *
     * @param args
     */
    public static void keytool(String[] args) {
        try {
            sun.security.tools.keytool.Main.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates BCFKS keystore with kc.db-password(alias) key containing password for a database.
     *
     * @throws Exception
     */
    public static void keystoreDemo() throws Exception {

        try {
            File f = new File(keyStoreFileName);
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        KeyStore store = KeyStore.getInstance("BCFKS", "BCFIPS");
        store.load(null, null);

        SecretKeySpec aesKey = new SecretKeySpec(dbPassword.getBytes(StandardCharsets.UTF_8), "AES");
        store.setKeyEntry(entryAlias, aesKey, "secretPwd1".toCharArray(), null);

        try (FileOutputStream fileOutputStream = new FileOutputStream(keyStoreFileName)) {
            store.store(fileOutputStream, keystorePassword.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Not used, just for debugging
     * @throws Exception
     */
    public static void providerInspection() throws Exception {

        Provider provider = new BouncyCastleFipsProvider();
        for (String k : provider.stringPropertyNames()) {
            if (k.startsWith("SecretKeyFactory") && k.toLowerCase().indexOf("pbe") >= 0) {
                System.out.println(k);
            }
        }

        Security.insertProviderAt(provider, 1);

    }


}
