package IO;

import java.io.*;
import java.nio.file.NotDirectoryException;

public class FileUtility {

    public static void createFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public static void createDirectory(String dirName) {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
        }
    }


    public static void move(File dir, File file) throws IOException {
        String path = dir.getAbsolutePath() + "/" + file.getName();
        createFile(path);
        InputStream is = new FileInputStream(file);
        try(OutputStream os = new FileOutputStream(new File(path))) {
            byte [] buffer = new byte[8192];
            while (is.available() > 0) {
                int readBytes = is.read(buffer);
                System.out.println(readBytes);
                os.write(buffer, 0, readBytes);
            }
        }
    }

    public static void getAllFiles(File root, StringBuilder sb) throws NotDirectoryException {
        File[] filesList = root.listFiles();
        if (filesList == null) {
            throw new NotDirectoryException("Error: Not a directory -- " + root);
        }
        for(File f : filesList){
            if(f.isDirectory())
                getAllFiles(f, sb);
            if(f.isFile()){
                sb.append(f.getName()).append('|');
            }
        }
    }

}
