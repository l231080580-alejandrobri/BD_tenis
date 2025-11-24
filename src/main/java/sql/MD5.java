
package sql;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.JTextField;

public class MD5 {
     public static String hashMD5(String input) {
        try {
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            byte[] hashBytes = md.digest(input.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();//.substring(0, 20);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 no disponible", e);
        }
    }

    
}
