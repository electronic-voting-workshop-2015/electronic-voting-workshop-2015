package OtherTeams;
import java.util.Map;

public interface ClientCryptographyModule {

    /**
     * Encrypts the message using ElGamal.
     * @param publicKey The key published by the BB.
     * @param message The message to encrypt.
     * @return A byte array containing the ciphertext.
     */
    byte[] encrypt(byte[] publicKey, byte[] message);

    /**
     * Create the signature passed to the BB using Diffie-Helman.
     * @param privateKey The party's key, given physically by the infrastructure team.
     * @param encryptedMessage
     * @return The signature's ciphertext.
     */
    byte[] sign(byte[] privateKey, byte[] encryptedMessage);

    /**
     * Verify a certificate of an encrypted message.
     * @param publicKey The signing party's public key.
     * @param encryptedMessage
     * @param certificate
     */
    boolean verifyCertificate(
            byte[] publicKey, byte[] encryptedMessage, byte[] certificate);

    /**
     * Returns a mapping of integers (candidate numbers) to group members in byte[] format.
     * @return
     */
    Map<Integer, byte[]> getCandidateToMemebrMapping(int candidateNum);
}