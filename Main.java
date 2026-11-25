
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {

        // Inscription des utilisateurs
        Conn auth = new Conn();

        auth.inscription("roman", "MonSuperMdp1!");
        auth.inscription("Natalia", "MonSuperMdp1!");

        // Configuration JAAS
        Configuration config = new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return new AppConfigurationEntry[]{
                        new AppConfigurationEntry(
                                LoginModule.class.getName(),
                                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                                Map.of()
                        )
                };
            }
        };

        // Tests JAAS
        Utilisateur utilisateur =  testerConnexion(config,"Enzo","MonSuperMdp1!");
        testerEchecAuthentification(config);

        // Tests de gestion des fichiers
        testerEnregistrement(utilisateur);
        testerConversion();
        testerListeFichiers();
    }

    // ==========================================
    // TESTS JAAS
    // ==========================================

    public static Utilisateur testerConnexion(Configuration config,String pseudo, String mdp) throws Exception {

        System.out.println("\n=== TESTS DE CONNEXION JAAS ===");

        LoginContext lc = new LoginContext(
                "TestApp",
                new Subject(),
                creerHandler(pseudo, mdp),
                config
        );

        try {
            lc.login();
        } catch (LoginException e) {
            verifier("Connexion valide avec " + pseudo, false);
            System.out.println("Cause : " + e.getMessage());
            throw e;
        }

        verifier("Connexion valide avec "+pseudo, true);

        Subject subject = lc.getSubject();

        boolean principalAjoute = subject.getPrincipals().stream()
                .anyMatch(p -> p.getName().equals(pseudo));

        verifier("Principal roman ajouté au Subject", principalAjoute);

        if (!principalAjoute) {
            lc.logout();
            throw new IllegalStateException("Le LoginModule n'a ajouté aucun Principal.");
        }

        Principal principal = subject.getPrincipals()
                .iterator()
                .next();

        Utilisateur utilisateur = new Utilisateur(principal.getName());

        // On ne déconnecte pas encore : on conserve la session.
        return utilisateur;
    }
    public static void testerEchecAuthentification(Configuration config) {

        System.out.println("\n=== TESTS D'ÉCHEC D'AUTHENTIFICATION ===");

        verifier(
                "Mauvais mot de passe refusé",
                testerEchec("roman", "MauvaisMdp1!", config)
        );

        verifier(
                "Utilisateur inconnu refusé",
                testerEchec("Inconnu", "MonSuperMdp1!", config)
        );

        verifier(
                "Identifiants null refusés",
                testerEchec(null, null, config)
        );
    }

    public static CallbackHandler creerHandler(String pseudo, String mdp) {

        return callbacks -> {
            for (Callback cb : callbacks) {

                if (cb instanceof NameCallback nc) {
                    nc.setName(pseudo);

                } else if (cb instanceof PasswordCallback pc) {
                    pc.setPassword(mdp != null ? mdp.toCharArray() : null);
                }
            }
        };
    }

    public static boolean testerEchec(
            String pseudo,
            String mdp,
            Configuration config
    ) {
        try {
            LoginContext lc = new LoginContext(
                    "TestApp",
                    new Subject(),
                    creerHandler(pseudo, mdp),
                    config
            );

            lc.login();
            lc.logout();

            return false;

        } catch (LoginException e) {
            return true;
        }
    }

    // ==========================================
    // TESTS DE GESTION DES FICHIERS
    // ==========================================

    public static void testerEnregistrement(Utilisateur utilisateur) throws IOException {

        System.out.println("\n=== TESTS DES FICHIERS ===");

        Fichier gestion = new Fichier();

        File dossier = new File("roman");

        byte[] contenu = "Bonjour axel ca va !".getBytes(StandardCharsets.UTF_8);

        File fichier = gestion.enregistrer(
                contenu,
                "axel.txt",
                utilisateur
        );

        verifier("Enregistrement du fichier", fichier.exists());
    }

    public static void testerConversion() throws IOException {

        Fichier gestion = new Fichier();

        File fichier = new File("utilisateur/roman/test.txt");

        byte[] resultat = gestion.convertirFichierVersByte(fichier);

        String texte = new String(resultat, StandardCharsets.UTF_8);

        verifier(
                "Conversion du fichier en bytes",
                texte.equals("Bonjour Roman !")
        );
    }

    public static void testerListeFichiers() {

        Fichier gestion = new Fichier();

        Utilisateur utilisateur = new Utilisateur("Enzo");

        File[] fichiers = gestion.list(utilisateur);
        for (File fichier : fichiers) {
            System.out.println("- " + fichier.getName());
        }
        boolean present = fichiers != null &&
                Arrays.stream(fichiers)
                        .anyMatch(f -> f.getName().equals("test.txt"));

        verifier("Récupération des fichiers utilisateur", present);
    }

    // ==========================================
    // OUTIL DE VÉRIFICATION
    // ==========================================

    public static void verifier(String nomDuTest, boolean condition) {

        if (condition) {
            System.out.println("[OK]     " + nomDuTest);
        } else {
            System.out.println("[ERREUR] " + nomDuTest);
        }
    }
}
