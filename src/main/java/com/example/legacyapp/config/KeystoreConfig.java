package com.example.legacyapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;

@Configuration
public class KeystoreConfig {

    @Value("${keystore.path:keystore/app-keystore.jks}")
    private String keystorePath;

    @Value("${keystore.password:changeit}")
    private String keystorePassword;

    @Value("${keystore.alias:app-cert}")
    private String keystoreAlias;

    private KeyStore keyStore;

    @PostConstruct
    public void initKeystore() {
        try {
            File keystoreFile = new File(keystorePath);
            keystoreFile.getParentFile().mkdirs();

            keyStore = KeyStore.getInstance("JKS");

            if (keystoreFile.exists()) {
                try (FileInputStream fis = new FileInputStream(keystoreFile)) {
                    keyStore.load(fis, keystorePassword.toCharArray());
                    System.out.println("Loaded existing keystore from: " + keystorePath);
                }
            } else {
                keyStore.load(null, keystorePassword.toCharArray());
                generateSelfSignedCertificate();
                saveKeystore();
                System.out.println("Created new keystore at: " + keystorePath);
            }

            listKeystoreEntries();
        } catch (Exception e) {
            System.err.println("Error initializing keystore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateSelfSignedCertificate() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String distinguishedName = "CN=LegacyApp, O=Example Organization, L=City, ST=State, C=US";
        
        long validity = 365L * 24 * 60 * 60 * 1000;
        long notBefore = System.currentTimeMillis();
        long notAfter = notBefore + validity;

        sun.security.x509.X509CertInfo certInfo = new sun.security.x509.X509CertInfo();
        certInfo.set(sun.security.x509.X509CertInfo.VERSION, 
                    new sun.security.x509.CertificateVersion(sun.security.x509.CertificateVersion.V3));
        certInfo.set(sun.security.x509.X509CertInfo.SERIAL_NUMBER, 
                    new sun.security.x509.CertificateSerialNumber(new java.math.BigInteger(64, new SecureRandom())));
        certInfo.set(sun.security.x509.X509CertInfo.ALGORITHM_ID,
                    new sun.security.x509.CertificateAlgorithmId(
                        sun.security.x509.AlgorithmId.get("SHA256withRSA")));
        certInfo.set(sun.security.x509.X509CertInfo.SUBJECT, 
                    new sun.security.x509.X500Name(distinguishedName));
        certInfo.set(sun.security.x509.X509CertInfo.ISSUER, 
                    new sun.security.x509.X500Name(distinguishedName));
        certInfo.set(sun.security.x509.X509CertInfo.KEY, 
                    new sun.security.x509.CertificateX509Key(keyPair.getPublic()));
        certInfo.set(sun.security.x509.X509CertInfo.VALIDITY,
                    new sun.security.x509.CertificateValidity(
                        new java.util.Date(notBefore), 
                        new java.util.Date(notAfter)));

        sun.security.x509.X509CertImpl cert = new sun.security.x509.X509CertImpl(certInfo);
        cert.sign(keyPair.getPrivate(), "SHA256withRSA");

        Certificate[] chain = new Certificate[] { cert };
        keyStore.setKeyEntry(keystoreAlias, keyPair.getPrivate(), 
                           keystorePassword.toCharArray(), chain);

        SecretKey apiKey = generateSecretKey();
        keyStore.setKeyEntry("api-key",
                           apiKey,
                           keystorePassword.toCharArray(), null);

        SecretKey dbPasswordKey = new SecretKeySpec("mySecureDBPassword123".getBytes(), "AES");
        keyStore.setKeyEntry("db-password",
                           dbPasswordKey,
                           keystorePassword.toCharArray(), null);
    }

    private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    private void saveKeystore() throws Exception {
        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            keyStore.store(fos, keystorePassword.toCharArray());
        }
    }

    private void listKeystoreEntries() throws KeyStoreException {
        System.out.println("Keystore entries:");
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            System.out.println("  - " + alias + " (" + 
                             (keyStore.isKeyEntry(alias) ? "key" : "certificate") + ")");
        }
    }

    @Bean
    public KeyStore getKeyStore() {
        return keyStore;
    }

    public String getSecret(String alias) {
        try {
            Key key = keyStore.getKey(alias, keystorePassword.toCharArray());
            if (key != null) {
                byte[] keyBytes = key.getEncoded();
                return Base64.getEncoder().encodeToString(keyBytes);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving secret: " + e.getMessage());
        }
        return null;
    }

    public void storeSecret(String alias, String secret) {
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes(), "AES");
            keyStore.setKeyEntry(alias,
                               secretKey,
                               keystorePassword.toCharArray(), null);
            saveKeystore();
        } catch (Exception e) {
            System.err.println("Error storing secret: " + e.getMessage());
        }
    }

    public Certificate getCertificate(String alias) {
        try {
            return keyStore.getCertificate(alias);
        } catch (KeyStoreException e) {
            System.err.println("Error retrieving certificate: " + e.getMessage());
            return null;
        }
    }
}