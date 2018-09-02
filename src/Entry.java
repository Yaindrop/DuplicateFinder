import java.io.*;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.regex.*;

public class Entry {
    private static List<String> getQuotedStr(String input) {
        Matcher m = Pattern.compile("\"(.*?)\"").matcher(input);
        List<String> res = new ArrayList<>();
        while (m.find()) {
            String s = m.group();
            s = s.substring(1, s.length() - 1);
            res.add(s);
        }
        return res;
    }
    private static String getTrace(Throwable t) {
        StringWriter sw= new StringWriter();
        PrintWriter pw= new PrintWriter(sw);
        t.printStackTrace(pw);
        StringBuffer buffer= sw.getBuffer();
        return buffer.toString();
    }
    public static void main(String[] args) {
        System.out.println("\n - Duplicate File Finder - \n");
        System.out.println("!!! Backup your files first !!! \n");
        System.out.println("Usage: ");
        System.out.println("\tscan \"rootPath\" \t\t--to scan all duplicated files under rootPath");
        System.out.println("\tdetail \t\t\t\t\t--to show all duplicated files under rootPath in detail");
        System.out.println("\tclean \t\t\t\t\t--to clean all duplicated files under rootPath (irrecoverable, not recommended)");
        System.out.println("\tgather \"gatherPath\" \t--to move all duplicated files under rootPath to gatherPath (recommended)");
        System.out.println("\tq \t\t\t\t\t\t--to end the program");
        System.out.println("\nEnter your commands: \n");
        String input;
        Scanner sc = new Scanner(System.in);
        RootedDuplicateFinder finder = null;
        while (!(input = sc.nextLine()).matches("^\\s?q\\s?$")) {
            try {
                if (input.matches("^scan\\s\".+\"")) {
                    System.out.println("Start scaning. This may take time ...");
                    String rootPath = getQuotedStr(input).get(0);
                    if (finder == null || !finder.getRootPath().equals(rootPath))
                        finder = new RootedDuplicateFinder(rootPath);
                    System.out.println(finder.getFileNum() + " files and " + finder.getFolderNum() + " folders scanned");
                    System.out.println(finder.getDupSetNum() + " sets of " + finder.getDupFileNum() + " duplicated files found");
                } else if (input.matches("^\\s?detail\\s?$")) {
                    if (finder == null) System.out.println("Please scan first");
                    else finder.printDetail();
                } else if (input.matches("^\\s?clean\\s?$")) {
                    if (finder == null) System.out.println("Please scan first");
                    else {
                        System.out.println("Start cleaning");
                        finder.clean();
                        finder = null;
                    }
                } else if (input.matches("^gather\\s\".+\"")) {
                    if (finder == null) System.out.println("Please scan first");
                    else {
                        System.out.println("Start gathering");
                        finder.setGather(getQuotedStr(input).get(0));
                        finder.gather();
                        finder = null;
                    }
                } else {
                    System.out.println("Invalid command");
                }
            } catch (InvalidPathException | NullPointerException e) {
                System.out.println("Program failed: " + getTrace(e));
            }
        }
    }
}
