import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.List;

public class Conn {

    private static final int ITERATIONS = 65536;
    private static final int LONGUEUR_CLE = 256;

    public boolean inscription(String pseudo, String mdp) throws Exception {
        if (!estPresent(pseudo) && estFort(mdp)) {
            SecureRandom random = new SecureRandom();
            byte[] sel = new byte[16];
            random.nextBytes(sel);

            String hashMdp = hacherMotDePasse(mdp, sel);
            String selBase64 = Base64.getEncoder().encodeToString(sel);

            Path cheminFichier = Path.of("utilisateurs.txt");
            String line = pseudo + ":" + selBase64 + ":" + hashMdp + System.lineSeparator();

            Files.writeString(
                    cheminFichier,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            return true;
        }
    return false;
    }

    public boolean connexion(String pseudo, String mdp) {
        Path cheminFichier = Path.of("utilisateurs.txt");
        if (!Files.exists(cheminFichier)) {
            return false;
        }

        try {
            List<String> lignes = Files.readAllLines(cheminFichier, StandardCharsets.UTF_8);
            for (String ligne : lignes) {
                String[] col = ligne.split(":");
                if (col[0].equals(pseudo)) {
                    if (col.length != 3) {
                        return false;
                    }
                    byte[] sel = Base64.getDecoder().decode(col[1]);
                    byte[] hashEnregistre = Base64.getDecoder().decode(col[2]);
                    String hashSaisiBase64 = hacherMotDePasse(mdp, sel);
                    byte[] hashSaisi = Base64.getDecoder().decode(hashSaisiBase64);
                    return MessageDigest.isEqual(hashEnregistre, hashSaisi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean estPresent(String pseudo) throws IOException {
        Path cheminFichier = Path.of("utilisateurs.txt");
        if (!Files.exists(cheminFichier)) {
            return false;
        }

        List<String> lignes = Files.readAllLines(cheminFichier, StandardCharsets.UTF_8);
        for (String ligne : lignes) {
            String[] col = ligne.split(":");
            if (col[0].equals(pseudo)) {
                return true;
            }
        }
        return false;
    }

    private String hacherMotDePasse(String mdp, byte[] sel) throws Exception {
        KeySpec spec = new PBEKeySpec(mdp.toCharArray(), sel, ITERATIONS, LONGUEUR_CLE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    public boolean estFort(String mdp) {
        if (mdp == null || mdp.length() < 12) {
            return false;
        }
        int spec = 0;
        int min = 0;
        int maj = 0;
        int chiffre = 0;

        for (int i = 0; i < mdp.length(); i++) {
            char c = mdp.charAt(i);
            if (Character.isDigit(c)) {
                chiffre++;
            } else {
                if (!Character.isLetterOrDigit(c)) {
                    spec++;
                } else {
                    if (Character.isUpperCase(c)) {
                        maj++;
                    } else if (Character.isLowerCase(c)) {
                        min++;
                    }
                }
            }
        }
        return chiffre >= 1 && maj >= 1 && min >= 1 && spec >= 1;
    }
}
