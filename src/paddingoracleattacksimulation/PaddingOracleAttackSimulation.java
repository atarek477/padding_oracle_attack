/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package paddingoracleattacksimulation;

/**
 * Disclaimer: This code is for illustration purposes. Do not use in real-world
 * deployments.
 */
public class PaddingOracleAttackSimulation {

    private static class Sender {

        private byte[] secretKey;
        private String secretMessage = "Top secret!";

        public Sender(byte[] secretKey) {
            this.secretKey = secretKey;
        }

        // This will return both iv and ciphertext
        public byte[] encrypt() {
            return AESDemo.encrypt(secretKey, secretMessage);
        }
    }

    private static class Receiver {

        private byte[] secretKey;

        public Receiver(byte[] secretKey) {
            this.secretKey = secretKey;
        }

        // Padding Oracle (Notice the return type)
        public boolean isDecryptionSuccessful(byte[] ciphertext) {
            return AESDemo.decrypt(secretKey, ciphertext) != null;
        }
    }

    public static class Adversary {

        // This is where you are going to develop the attack
        // Assume you cannot access the key. 
        // You shall not add any methods to the Receiver class.
        // You only have access to the receiver's "isDecryptionSuccessful" only. 
        public String extractSecretMessage(Receiver receiver, byte[] ciphertext) {

            byte[] iv = AESDemo.extractIV(ciphertext);
            byte[] ciphertextBlocks = AESDemo.extractCiphertextBlocks(ciphertext);
            boolean result = receiver.isDecryptionSuccessful(AESDemo.prepareCiphertext(iv, ciphertextBlocks));
            //System.out.println(result); // This is true initially, as the ciphertext was not altered in any way.

            // TODO: WRITE THE ATTACK HERE
            int b = 0;  // padding
            String Message = "";
            for (int i = 0; i < iv.length; i++) {
                iv[i]++;
                result = receiver.isDecryptionSuccessful(AESDemo.prepareCiphertext(iv, ciphertextBlocks));
                System.out.println("here byte no  " + i + "  " + result);
                if (result == false) {
                    b = ciphertextBlocks.length - (i % ciphertextBlocks.length);           //  b= L - (X MOD L) 
                    System.out.println("padding " + b);
                    break;
                }
            }
            iv = AESDemo.extractIV(ciphertext);        // reset iv 
            for (int x = b; x < iv.length; x++) {
                int count = 0;
                for (int i = iv.length - 1; i >= (iv.length - b); i--) {
                    iv[i] = (byte) (b ^ iv[i] ^ (b + 1));
                    count++;
                }
                count = (iv.length - count) - 1;
                 for (byte i =-128; i < 127; i++) {
                    iv[count] =  i;
                    result = receiver.isDecryptionSuccessful(AESDemo.prepareCiphertext(iv, ciphertextBlocks));
                    if (result == true) {
                        Message = Message.concat(Character.toString((char) (iv[count] ^ (b + 1) ^ ciphertext[count])));
                        break;
                    }
                }
                b++;
            }
            String FinalMessage = new StringBuffer(Message).reverse().toString();
            return FinalMessage;
        }
    }

    public static void main(String[] args) {

        byte[] secretKey = AESDemo.keyGen();
        Sender sender = new Sender(secretKey);
        Receiver receiver = new Receiver(secretKey);

        // The adversary does not have the key
        Adversary adversary = new Adversary();

        // Now, let's get some valid encryption from the sender
        byte[] ciphertext = sender.encrypt();

        // The adversary  got the encrypted message from the network.
        // The adversary's goal is to extract the message without knowing the key.
        String message = adversary.extractSecretMessage(receiver, ciphertext);

        System.out.println("Extracted message = " + message);
    }
}
