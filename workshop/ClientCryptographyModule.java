package workshop;

public interface ClientCryptographyModule {

    /**
     * Encrypts the message using ElGamal.
     * @param publicKey The key published by the BB.
     * @param message The message to encrypt.
     * @return A byte array containing the ciphertext.
     */
    byte[] encrypt(byte[] publicKey, byte[] message);

    /**
     * Create the signatue passed to the BB using Diffie-Helman.
     * @param privateKey The party's key, given physically by the infrastructure team.
     * @param message The message to encrypt.
     * @return The signature's ciphertext.
     */
    byte[] sign(byte[] privateKey, byte[] message);

    /**
     * Verify a certificate of an encrypted message.
     * @param publicKey The signing party's public key.
     * @param encryptedMessage
     * @param certificate
     */
    boolean verifyCertificate(
            byte[] publicKey, byte[] encryptedMessage, byte[] certificate);
}
