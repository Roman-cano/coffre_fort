import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FichierInterface {

    public File[] list(Utilisateur utilisateur);
    public byte[] convertirFichierVersByte(File file) throws IOException;
    public File enregistrer(byte[] contenu, String nom, Utilisateur utilisateur) throws IOException;
    public File supprimer(String nom, Utilisateur utilisateur);

}
