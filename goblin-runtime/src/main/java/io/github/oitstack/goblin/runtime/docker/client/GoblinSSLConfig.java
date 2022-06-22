/*
 * Copyright 2022 OPPO Goblin Project
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
package io.github.oitstack.goblin.runtime.docker.client;

import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.core.SSLConfig;
import com.github.dockerjava.core.util.CertificateUtils;
import io.github.oitstack.goblin.runtime.docker.utils.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

/**
 * Goblin docker client ssl configuration management class.
 */
public class GoblinSSLConfig implements SSLConfig, Serializable {
    private static final long serialVersionUID = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(GoblinSSLConfig.class);
    private static final String DEFAULT_CERT_PATH = "/cert";

    private static final String CA_FILE_NAME = "ca.pem";
    private static final String KEY_FILE_NAME = "key.pem";
    private static final String CERT_FILE_NAME = "cert.pem";
    public static final String DOCKER = "docker";
    public static final String SSL_KEY_MANAGER_FACTORY_ALGORITHM = "ssl.keyManagerFactory.algorithm";
    public static final String SSL_TRUST_MANAGER_FACTORY_ALGORITHM = "ssl.trustManagerFactory.algorithm";

    /**
     * Location of Docker client certificate.
     */
    private String dockerCertPath;

    public GoblinSSLConfig(String dockerCertPath) {
        if (StringUtils.isBlank(dockerCertPath)) {
            dockerCertPath = DEFAULT_CERT_PATH;
        }
        this.dockerCertPath = dockerCertPath;
    }

    /**
     * Get the SSL Context from file.
     * @return an SSL context.
     */
    @Override
    public SSLContext getSSLContext() {
        URL resource = GoblinSSLConfig.class.getResource(this.dockerCertPath);
        if (resource == null) {
            LOGGER.warn("there is no configured certificate directory of docker client, dir={}.", dockerCertPath);
            return null;
        }

        if (CertificateUtils.verifyCertificatesExist(resource.getPath())) {
            try {
                Security.addProvider(new BouncyCastleProvider());
                String capem = readToString(this.dockerCertPath + "/" + CA_FILE_NAME);
                String keypem = readToString(this.dockerCertPath + "/" + KEY_FILE_NAME);
                String certpem = readToString(this.dockerCertPath + "/" + CERT_FILE_NAME);

                KeyManagerFactory keyManagerFactory = initKeyManagerFactory(keypem, certpem);
                TrustManagerFactory trustManagerFactory = initTrustManagerFactory(capem);
                SSLContext sslContext = initSSLContext(keyManagerFactory, trustManagerFactory);
                return sslContext;
            } catch (Exception var13) {
                throw new DockerClientException(var13.getMessage(), var13);
            }
        } else {
            LOGGER.warn("there is no certificates of docker client in path {}.", dockerCertPath);
            return null;
        }
    }

    private SSLContext initSSLContext(KeyManagerFactory keyManagerFactory, TrustManagerFactory trustManagerFactory) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    private TrustManagerFactory initTrustManagerFactory(String capem) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException {
        String tmfAlgorithm = AccessController.doPrivileged(this.getSystemProperty(SSL_TRUST_MANAGER_FACTORY_ALGORITHM, TrustManagerFactory.getDefaultAlgorithm()));
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
        trustManagerFactory.init(CertificateUtils.createTrustStore(capem));
        return trustManagerFactory;
    }

    private KeyManagerFactory initKeyManagerFactory(String keypem, String certpem) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, InvalidKeySpecException, IOException, CertificateException {
        String kmfAlgorithm = AccessController.doPrivileged(this.getSystemProperty(SSL_KEY_MANAGER_FACTORY_ALGORITHM, KeyManagerFactory.getDefaultAlgorithm()));
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(kmfAlgorithm);
        keyManagerFactory.init(CertificateUtils.createKeyStore(keypem, certpem), DOCKER.toCharArray());
        return keyManagerFactory;
    }

    private PrivilegedAction<String> getSystemProperty(String name, String def) {
        return () -> System.getProperty(name, def);
    }

    public static String readToString(String filePath) {
        File file = new File(GoblinSSLConfig.class.getResource(filePath).getPath());
        Long fileLength = file.length();
        byte[] fileContent = new byte[fileLength.intValue()];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(fileContent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(fileContent);
    }

}
