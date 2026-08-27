import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class GenerateKeys {

    public static void main(String[] args) {

        try {

            // =====================================================
            // 1. Create keys directory
            // =====================================================

            Path keysDirectory = Path.of("keys");

            Files.createDirectories(keysDirectory);


            // =====================================================
            // 2. Generate RSA 2048-bit key pair
            // =====================================================

            KeyPairGenerator keyPairGenerator =
                    KeyPairGenerator.getInstance("RSA");

            keyPairGenerator.initialize(2048);

            KeyPair keyPair =
                    keyPairGenerator.generateKeyPair();


            // =====================================================
            // 3. Convert PRIVATE key to PKCS#8 PEM format
            // =====================================================

            String privateKeyBase64 =
                    Base64.getMimeEncoder(
                            64,
                            "\n".getBytes(StandardCharsets.UTF_8)
                    ).encodeToString(
                            keyPair.getPrivate().getEncoded()
                    );

            String privateKeyPem =
                    "-----BEGIN PRIVATE KEY-----\n"
                            + privateKeyBase64
                            + "\n-----END PRIVATE KEY-----\n";


            // =====================================================
            // 4. Convert PUBLIC key to X.509 PEM format
            // =====================================================

            String publicKeyBase64 =
                    Base64.getMimeEncoder(
                            64,
                            "\n".getBytes(StandardCharsets.UTF_8)
                    ).encodeToString(
                            keyPair.getPublic().getEncoded()
                    );

            String publicKeyPem =
                    "-----BEGIN PUBLIC KEY-----\n"
                            + publicKeyBase64
                            + "\n-----END PUBLIC KEY-----\n";


            // =====================================================
            // 5. Save PRIVATE key
            // =====================================================

            Path privateKeyPath =
                    keysDirectory.resolve("private.pem");

            Files.writeString(
                    privateKeyPath,
                    privateKeyPem,
                    StandardCharsets.UTF_8
            );


            // =====================================================
            // 6. Save PUBLIC key
            // =====================================================

            Path publicKeyPath =
                    keysDirectory.resolve("public.pem");

            Files.writeString(
                    publicKeyPath,
                    publicKeyPem,
                    StandardCharsets.UTF_8
            );


            // =====================================================
            // 7. Print result
            // =====================================================

            System.out.println();
            System.out.println("========================================");
            System.out.println("       ECHOLIFE RSA KEYS GENERATED");
            System.out.println("========================================");
            System.out.println();

            System.out.println(
                    "Private Key : " + privateKeyPath.toAbsolutePath()
            );

            System.out.println(
                    "Public Key  : " + publicKeyPath.toAbsolutePath()
            );

            System.out.println();
            System.out.println("RSA Algorithm : RSA");
            System.out.println("Key Size      : 2048 bits");
            System.out.println();
            System.out.println("Generation completed successfully.");
            System.out.println("========================================");
            System.out.println();

        } catch (Exception e) {

            System.err.println();
            System.err.println("========================================");
            System.err.println("       KEY GENERATION FAILED");
            System.err.println("========================================");

            e.printStackTrace();
        }
    }
}