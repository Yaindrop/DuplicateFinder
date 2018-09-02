import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.util.*;

public class RootedDuplicateFinder {
    private int fileNum, folderNum;
    private File root = null, gather = null;
    private HashMap<File, String> file2Id = new HashMap<>();
    private HashMap<File, HashSet<File>> dupSets = new HashMap<>();
    RootedDuplicateFinder(String r) {
        setRoot(r);
    }
    public void setRoot(String r) {
        File temp = new File(r);
        if (!temp.exists() || !temp.isDirectory()) throw new InvalidPathException(r, "Invalid root path");
        root = temp;
        System.out.println("Calculating ids");
        fileNum = folderNum = 0;
        LinkedList<File> folders = new LinkedList<>();
        folders.add(root);
        int n = 0;
        while (!folders.isEmpty()) {
            File[] files = folders.peekFirst().listFiles();
            if (files != null && files.length != 0) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        folders.addLast(f);
                        folderNum ++;
                    } else try {
                        FileInputStream fis1 = new FileInputStream(f);
                        FileInputStream fis2 = new FileInputStream(f);
                        file2Id.put(f, DigestUtils.md5Hex(fis1) + DigestUtils.sha1Hex(fis2));
                        fis1.close();
                        fis2.close();
                        fileNum ++;
                        if (++n % 100 == 0) System.out.println(n + " files processed");
                    } catch (Exception e) {
                        System.out.println("Unexpected Exception @" + f.getPath());
                    }
                }
            }
            folders.removeFirst();
        }
        System.out.println("Checking duplication");
        n = 0;
        HashMap<String, File> id2File = new HashMap<>();
        for (File f : file2Id.keySet()) {
            String id = file2Id.get(f);
            if (!id2File.containsKey(id)) id2File.put(id, f);
            else {
                File prev = id2File.get(id);
                if (!dupSets.containsKey(prev)) dupSets.put(prev, new HashSet<>());
                dupSets.get(prev).add(f);
            }
            if (++n % 10000 == 0) System.out.println(n + " files checked");
        }
        // Keep file with shorter/alphabetically smaller name
        HashMap<File, HashSet<File>> newDupSets = new HashMap<>();
        for (File f : dupSets.keySet()) {
            File keep = f;
            for (File d : dupSets.get(f)) if (d.getName().length() < keep.getName().length()
                    || (d.getName().length() == keep.getName().length()) && d.getName().compareTo(keep.getName()) < 0) keep = d;
            if (keep == f) newDupSets.put(f, dupSets.get(f));
            else {
                System.out.println(keep.getName());
                newDupSets.put(keep, dupSets.get(f));
                newDupSets.get(keep).add(f);
                newDupSets.get(keep).remove(keep);
            }
        }
        dupSets = newDupSets;
    }
    public void setGather(String g) {
        File temp = new File(g);
        if (!temp.exists())
            if (temp.mkdirs()) System.out.println("Created gathering path: " + temp.getPath());
            else throw new InvalidPathException(g, "Invalid gathering path");
        gather = temp;
    }
    public int getFileNum() {
        return fileNum;
    }
    public int getFolderNum() {
        return folderNum;
    }
    public String getRootPath() {
        return root.getPath();
    }
    public int getDupSetNum() {
        return dupSets.keySet().size();
    }
    public int getDupFileNum() {
        int res = 0;
        for (File k : dupSets.keySet()) res += dupSets.get(k).size();
        return res;
    }
    public void printDetail() {
        int n = 0, l = root.getPath().length();
        for (File k : dupSets.keySet()) {
            System.out.print("Set " + ++n + ": ..." + k.getPath().substring(l));
            for (File d : dupSets.get(k)) System.out.print(", ..." + d.getPath().substring(l));
            System.out.println();
        }
    }
    public void clean() {
        for (File k : dupSets.keySet()) for (File d : dupSets.get(k)) if (d.delete()) System.out.println("Deleted: " + d.getPath());
    }
    public void gather() {
        if (gather == null) throw new NullPointerException("No gathering path");
        int l = root.getPath().length();
        for (File k : dupSets.keySet()) for (File d : dupSets.get(k)) if (move(d, new File(gather.getPath() + d.getPath().substring(l))))
            System.out.println("Gathered: " + d.getPath());
    }
    private boolean move(File from, File to) {
        try {
            if(!to.getParentFile().exists()) if (!to.getParentFile().mkdirs()) return false;
            return from.renameTo(to);
        } catch (Exception e) {
            return false;
        }
    }
}
