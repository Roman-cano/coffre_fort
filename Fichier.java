import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Fichier implements FichierInterface {
    @Override
    public File[] list(Utilisateur utilisateur){
        File dossier = new File("utilisateur/" + utilisateur.getName());
        File[] contenu = dossier.listFiles();

        return contenu;

    }

    @Override
    public byte[] convertirFichierVersByte(File fichier) throws IOException {
        return Files.readAllBytes(fichier.toPath());

    }


    @Override
    public File enregistrer(byte[] contenu, String nom, Utilisateur utilisateur) throws IOException {

        File dossier = new File("utilisateur/" + utilisateur.getName());

        if (!dossier.exists() && !dossier.mkdirs()) {
            throw new IOException("Impossible de créer le dossier utilisateur");
        }

        File fichier = new File(dossier, nom);

        Files.write(fichier.toPath(), contenu);

        return fichier;
    }

    @Override
    public File supprimer(String nom, Utilisateur utilisateur) {
        String nomUti = utilisateur.getName();
        return null;
    }

}
