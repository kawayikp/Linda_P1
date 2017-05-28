import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class InputOutputController {
    public static void generateFile(String[] paths, String path, String netsFile, String tuplesFile) {
        File dir = new File(path);
        dir.mkdirs();
        dir = new File(path + netsFile); // if exists, will do nothing
        try {
            dir.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        dir = new File(path);
        dir.mkdirs();
        dir = new File(path + tuplesFile); // if exists, will do nothing
        try {
            dir.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int i = 0; i < paths.length; i++) {
            dir = new File(paths[i]);
            dir.setReadable(true, false);
            dir.setWritable(true, false);
            dir.setExecutable(true, false);
        }

        dir = new File(path + netsFile);
        dir.setReadable(true, false);
        dir.setWritable(true, false);

        dir = new File(path + tuplesFile);
        dir.setReadable(true, false);
        dir.setWritable(true, false);
    }

    // todo
    public static void removePath() {

    }

    public static <E> void serialize(E obj, String filename) {
        FileOutputStream fout = null;
        ObjectOutputStream out = null;
        try {
            fout = new FileOutputStream(filename);
            out = new ObjectOutputStream(fout);
            out.writeObject(obj);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> E deSerialize(String filename) {
        E transOb = null;
        FileInputStream fis = null;
        ObjectInputStream fin = null;
        try {
            fis = new FileInputStream(filename);
            fin = new ObjectInputStream(fis);
            transOb = (E) fin.readObject();
            fin.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return transOb;
    }
}
