# Keycloak Configuration Keystore Setup in FIPS mode using Bouncy Castle

This project illustrates how to create keystore of BCFKS type that can be used to supply secret values to Keycloak Server version 24.

## Prerequisites

1. JDK 17 - use project [SDKMAN](https://sdkman.io/) for easy installation
   - I am using 17.0.11-tem.
2. Make sure your operating system is configured and is in the FIPS more.
   - Fedora 40 can be checked using command `sudo fips-mode-setup --check`
3. Configure JDK to use Bouncy Castle BCFIPS security provider.
   - Do following changes in file `$JAVA_HOME/conf/security/java.security`
```
security.provider.1=org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider C:DEFRND[SHA256];ENABLE{ALL};
security.provider.2=com.sun.net.ssl.internal.ssl.Provider BCFIPS
security.provider.3=sun.security.provider.Sun

...

securerandom.strongAlgorithms=PKCS11:SunPKCS11-NSS-FIPS
```

## Setup

1. Run `mvn clean install` to compile this project.
2. Run `mvn exec:exec` to create `keystore.bcfks` keystore.
   - all parameters could be seen in [BCImportPass.java](https://github.com/pskopek/keycloak-fips-setup/blob/b631b14286c3a371b203cc0b1678ef03a3d53a2a/src/main/java/org/keycloak/fips/BCImportPass.java#L39)
   - all parameters are hardcoded for easy reference
3. Install [Keycloak Server version 24.0.5](https://github.com/keycloak/keycloak/releases/download/24.0.5/keycloak-24.0.5.zip) 
4. Configure database PostgreSQL [database for use with Keycloak](https://www.keycloak.org/server/db)
   - Do not forget to use password from step 2.
5. Add following `--add-opens` argument to server start script
```
--add-opens=java.base/sun.security.provider=ALL-UNNAMED
```
6. Set configuration to use keystore. `./bin/kc.sh build --vault=keystore`
7. Run this example to enable FIPS mode for Keycloak 
```
./bin/kc.sh start --hostname localhost --http-host localhost --http-enabled true --hostname-strict-https=false --db-url-host=localhost --db-username=keycloak --vault-file=keycloak-fips-setup/keystore.bcfks --vault-pass=secretPwd1 --vault-type=BCFKS --features=fips
```
8. To run the server next time use this command
```
./bin/kc.sh start --hostname localhost --http-host localhost --http-enabled true --hostname-strict-https=false --db-url-host=localhost --db-username=keycloak --vault-file=keycloak-fips-setup/keystore.bcfks --vault-pass=secretPwd1 --vault-type=BCFKS --optimized
```
9. Check if the running Keycloak Server works as expected and is connected to the database. 