package alexoft.Minebackup;
 

import java.io.*;
import java.util.zip.*;
 

/**
 * Classe d'utilitaires pour les compressions et décompression Zip, GZip, JAR...
 * @author iubito (Sylvain Machefert)
 */
public class ZipUtils {
 
    /** Taille du buffer pour les lectures/écritures */
    private static final int BUFFER_SIZE = 8 * 1024;
 
    /**
     * Décompresse un GZIP contenant un fichier unique.
     * 
     * Efface le filedest avant de commencer.
     * @param gzsource Fichier GZIP é décompresser
     * @param filedest Nom du fichier destination oé sera sauvegardé le fichier
     * contenu dans le GZIP.
     * @throws FileNotFoundException si le fichier GZip n'existe pas
     * @throws IOException 
     * @see http://javaalmanac.com/egs/java.util.zip/UncompressFile.html?l=rel
     */
    public static void gunzip(String gzsource, String filedest)
        throws FileNotFoundException, IOException {
        // Open the compressed file
        GZIPInputStream in = new GZIPInputStream(new FileInputStream(gzsource));

        try {
            BufferedInputStream bis = new BufferedInputStream(in);

            try {
                // Open the output file
                OutputStream out = new FileOutputStream(filedest);

                try {
                    BufferedOutputStream bos = new BufferedOutputStream(out);

                    try {
                        // Transfer bytes from the compressed file to the output file
                        byte[] buf = new byte[BUFFER_SIZE];
                        int len;

                        while ((len = bis.read(buf, 0, BUFFER_SIZE)) != -1) {
                            bos.write(buf, 0, len);
                        }
                        buf = null;
                    } finally {
                        bos.close();
                    }
                } finally {
                    out.close();
                }
            } finally {
                bis.close();
            }
        } finally {
            in.close();
        }
    }
 
    /**
     * Compresse un fichier dans un GZIP.
     * 
     * Efface le filedest avant de commencer.
     * @param filesource Fichier é compresser
     * @param gzdest Fichier GZIP cible
     * @throws FileNotFoundException si le fichier source n'existe pas ou si le GZIP
     * n'existe pas aprés la compression
     * @throws IOException 
     * @see http://javaalmanac.com/egs/java.util.zip/CompressFile.html?l=rel
     * @see http://java.developpez.com/livres/penserenjava/?chap=12&page=3
     */
    public static void gzip(String filesource, String gzdest)
        throws FileNotFoundException, IOException {
        // Create the GZIP output stream
        GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzdest));

        try {
            BufferedOutputStream bos = new BufferedOutputStream(out);

            try {
                // Open the input file
                FileInputStream in = new FileInputStream(filesource);

                try {
                    BufferedInputStream bis = new BufferedInputStream(in);

                    try {
                        // Transfer bytes from the input file to the GZIP output stream
                        byte[] buf = new byte[BUFFER_SIZE];
                        int len;

                        while ((len = bis.read(buf, 0, BUFFER_SIZE)) > 0) {
                            bos.write(buf, 0, len);
                        }
                        buf = null;
                    } finally {
                        bis.close();
                    }
                } finally {
                    in.close();
                }
            } finally {
                bos.close();
            }
        } finally {
            out.close();
        }
        if (!new File(gzdest).exists()) {
            throw new FileNotFoundException(
                    "Le fichier " + gzdest + " n'a pas été créé");
        }
    }
 
    /**
     * Décompresse l'archive Zip dans un répertoire.
     * Réutilise les noms des répertoires lors de la décompression.
     * @param zipsrc
     * @param basedirdest
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SecurityException
     */
    public static void unzipToDir(String zipsrc, String basedirdest)
        throws FileNotFoundException, IOException, SecurityException {
 
        unzipToDir(new FileInputStream(zipsrc), basedirdest);
    }
 
    /**
     * Décompresse le flux <tt>InputStream</tt> Zip dans un répertoire.
     * Réutilise les noms des répertoires lors de la décompression.
     * @param zipsrc
     * @param basedirdest
     * @throws SecurityException
     * @throws IOException
     */
    public static void unzipToDir(InputStream inzip, String basedirdest)
        throws IOException, SecurityException {
 
        File base = new File(basedirdest);

        if (!base.exists()) {
            base.mkdirs();
        }
 
        try {
            CheckedInputStream checksum = new CheckedInputStream(inzip,
                    new Adler32());

            try {
                // Buffer sur le zip
                BufferedInputStream bis = new BufferedInputStream(checksum);

                try {
                    ZipInputStream zis = new ZipInputStream(bis);

                    try {
                        ZipEntry entry;
                        File f;
                        int count;
                        byte[] buf = new byte[BUFFER_SIZE];
                        BufferedOutputStream bos;
                        FileOutputStream fos;

                        // Parcours les entrées du zip
                        while ((entry = zis.getNextEntry()) != null) {
                            f = new File(basedirdest, entry.getName());
                            if (entry.isDirectory()) {
                                f.mkdirs();
                            } else { // L'entry semble étre un fichier
                                // Si contient un / on crée les répertoires, car parfois on a pas dir/ avant dir/fichier.ext
                                int l = entry.getName().lastIndexOf('/');

                                if (l != -1) {
                                    new File(basedirdest, entry.getName().substring(0, l)).mkdirs();
                                }
                                fos = new FileOutputStream(f);
                                try {
                                    bos = new BufferedOutputStream(fos,
                                            BUFFER_SIZE);
                                    try {
                                        // Ecriture du fichier
                                        while ((count = zis.read(buf, 0,
                                                BUFFER_SIZE))
                                                != -1) {
                                            bos.write(buf, 0, count);
                                        }
                                    } finally {
                                        bos.close();
                                    }
                                } finally {
                                    fos.close();
                                }
                            }
                            if (entry.getTime() != -1) {
                                f.setLastModified(entry.getTime());
                            }
                        }
                    } finally {
                        zis.close();
                    }
                } finally {
                    bis.close();
                }
            } finally {
                checksum.close();
            }
        } finally {
            inzip.close();
        }
    }
 
    /**
     * Zippe récursivement un répertoire, en mettant les chemins de fichiers en relatif.
     * <p>Les accents des noms de fichiers sont supprimés, voir
     * {@link OutilsString#sansAccents(String)} pour plus de détails.
     * @param dirsource Le répertoire é compresser
     * @param zipdest Le nom du fichier zip résultat
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void zipDir(String dirsource, String zipdest, int method, int level)
        throws FileNotFoundException, IOException {
 
        // Création d'un flux d'écriture vers un fichier
        FileOutputStream fos = new FileOutputStream(zipdest);

        try {
            // Ajout du checksum : Adler32 (plus rapide) ou CRC32
            CheckedOutputStream checksum = new CheckedOutputStream(fos,
                    new Adler32());

            try {
                // Création d'un buffer de sortie afin d'améliorer les performances
                BufferedOutputStream bos = new BufferedOutputStream(checksum,
                        BUFFER_SIZE);

                try {
                    // Création d'un flux d'écriture Zip vers le fichier é travers le buffer
                    ZipOutputStream zos = new ZipOutputStream(bos);

                    try {
                        // Compression maximale
                        try {
                            zos.setMethod(method);
                            zos.setLevel(level);
                           
                            /* zos.setMethod(ZipOutputStream.DEFLATED);
                             zos.setLevel(Deflater.BEST_COMPRESSION);*/
                        } catch (Exception ignor) {}
                        zipDir(dirsource, null, zos);
                    } finally {
                        zos.close();
                    }
                } finally {
                    bos.close();
                }
            } finally {
                checksum.close();
            }
        } finally {
            fos.close();
        }
    }
 
    /**
     * étant donné un répertoire base (qui n'est pas inclu dans les entrées
     * <tt>ZipEntry</tt>), le répertoire courant é zipper, et le <tt>ZipOutputStream</tt>
     * de sortie, ajoute les fichiers dans le zip, ou s'appelle récursivement pour ajouter
     * un répertoire fils.
     * @param basedir
     * @param currentdir
     * @param zos
     * @throws FileNotFoundException
     * @throws IOException
     * @see http://www.developpez.net/forums/viewtopic.php?p=1724764
     */
    private static void zipDir(String basedir, String currentdir, ZipOutputStream zos)
        throws FileNotFoundException, IOException {
        // create a new File object based on the directory we have to zip
        File zipDir = (currentdir != null)
                ? new File(basedir, currentdir)
                : new File(basedir);
        // get a listing of the directory content
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[BUFFER_SIZE];
        int bytesIn = 0;
        // On met pas File.separator, mais "/" parce que c'est le caractére utilisé
        // dans les ZIP.
        String currentdir2 = (currentdir != null) ? (currentdir + "/") : "";
 
        File f;
        FileInputStream fis;
        BufferedInputStream bis;
        ZipEntry anEntry;

        // Create an empty entry with just dir name, like WinZip, so unzipToDir will
        // behave correctly.
        if (currentdir2.length() > 0) {
            anEntry = new ZipEntry(currentdir2);
            zos.putNextEntry(anEntry);
            zos.closeEntry();
        }
        // loop through dirList, and zip the files
        for (int i = 0; i < dirList.length; i++) {
            f = new File(zipDir, dirList[i]);
            if (!f.exists()) {
                continue;
            }
            if (f.isDirectory()) {
                // if the File object is a directory, call this
                // function again to add its content recursively
                zipDir(basedir, currentdir2 + dirList[i], zos);
                continue;
            }
            // if we reached here, the File object f was not a directory
            // create a FileInputStream on top of f
            fis = new FileInputStream(f);
            try {
                bis = new BufferedInputStream(fis, BUFFER_SIZE);
                try {
                    // create a new zip entry
                    anEntry = new ZipEntry((currentdir2 + dirList[i]));
                    anEntry.setTime(f.lastModified());
 
                    // place the zip entry in the ZipOutputStream object
                    zos.putNextEntry(anEntry);
                    // now write the content of the file to the ZipOutputStream
                    while ((bytesIn = bis.read(readBuffer, 0, BUFFER_SIZE))
                            != -1) {
                        zos.write(readBuffer, 0, bytesIn);
                    }
                    zos.closeEntry();
                } finally {
                    bis.close();
                }
            } finally {
                // close the Stream
                fis.close();
            }
        }
    }
	
}
