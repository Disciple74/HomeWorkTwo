package Epam;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.lang.System.*;

public class Console implements Runnable {
    private String path;

    Console(){
        this.path = "C:\\";
        out.println("Default directory set to " + this.path);
    }

    Console(String path){
        if (Files.isDirectory(Paths.get(path))) {
            this.path = path + "\\";
        } else {
            out.println("There's no such directory.");
            this.path = "C:\\";
        }
        pathNormalize();
        out.println("Default directory set to " + this.path);
    }

    @Override
    public void run() {
        location:
        while (true) {
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(in));
            try {
                out.print("$" + this.path + ">");
                String[] command = fromConsole.readLine().split(" ");
                switch (command[0]) {
                    case "exit":
                        break location;
                    case "copy":
                        if (copy(command)) out.println("Done!");
                        else out.println("Sorry, I tried really hard:(");
                        break;
                    case "make":
                        if (make(command)) out.println("Done");
                        else out.println("Sorry, I tried really hard:(");
                        break;
                    case "move":
                        if (copy(command)&&delete(new String[]{command[0], command[1]}))
                        break;
                        else out.println("Sorry, I tried really hard:(");
                    case "goto":
                        if (command.length >= 2) {
                            changeDir(command[1]);
                        } else changeDir(this.path);
                        break;
                    case "rename":
                        if (rename(command)) out.println("Done!");
                        else out.println("Sorry, i tried really hard:(");
                        break;
                    case "delete":
                        if (delete(command)) out.println("Done!");
                        else out.println("Sorry, I tried really hard:(");
                        break;
                    case "list":
                        if (command.length > 1){
                            StringBuilder dir = new StringBuilder();
                            for (int i = 1; i < command.length; i++){
                                dir.append(command[i]).append(" ");
                            }
                            if (dir.toString().contains(":")){
                                list(dir.insert(0, "\"").append("\"").toString());
                            } else list("\"" + this.path + dir.append("\"").toString());
                        }else list();
                        break;
                    case "open":
                        StringBuilder dir = new StringBuilder();
                        if (command.length > 1) {
                            for (int i = 1; i < command.length; i++) {
                                dir.append(command[i]).append("\\");
                            }
                        } else dir.append(command[1]);
                        if (dir.toString().contains(":")){
                            open(dir.toString());
                        } else open(this.path + dir.toString());
                        break;
                    case "help":
                        FileReader file;
                        try {
                            file = new FileReader(this.getClass().getResource("help.txt").getFile());
                            int c ;
                            while ((c = file.read()) != -1)
                                out.print((char)c);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "zip":
                        String[] tmp = this.path.split("\\\\");
                        int length = tmp.length > 1 ? tmp.length - 2 : tmp.length-1;
                        String name = "";
                        for (int i = 0; i <= length; i++) {
                             name += tmp[i]+"\\";
                        }
                        if (length == 0) name += "backup";
                        if (command.length > 1) {
                            name += command[1];
                            archivation(name);
                        } else {
                            name += tmp[tmp.length - 1];
                            archivation(name);
                        }
                        break;
                    case "unzip":
                        if (command.length < 2){
                            System.out.println("Wrong arguments. RTFM!");
                        } else if (command.length >= 2){
                            String zipName, folderName;
                            if (command[1].contains(":"))
                                zipName = command[1];
                            else zipName = this.path + command[1];
                            if(!Files.probeContentType(Paths.get(zipName)).contains("zip")){
                                System.out.println("It's not an archive!");
                                break;
                            }
                            if (command.length > 2){
                                folderName = (command[2].contains(":")) ? command[2] : this.path + command[2];
                            }else folderName = zipName.replace(".zip", "");
                            File archive = new File(zipName);
                            Files.deleteIfExists(Paths.get(folderName));
                            Files.createDirectory(Paths.get(folderName));
                            fromArchive(new FileInputStream(archive), folderName);
                        }
                        break;
                    default:
                        out.println("Can't recognize this command");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        out.println("Bye bye!");
    }
    private void changeDir(String pathTo) {
        if (pathTo.contains(":")) {
            if (Files.exists(Paths.get(pathTo)) && Files.isDirectory(Paths.get(pathTo))) {
                this.path = pathTo + "\\";
            } else out.println("Directory doesn't exists");
        } else if (Files.exists(Paths.get(this.path + pathTo))
                && Files.isDirectory(Paths.get(this.path + pathTo))) {
            this.path += pathTo + "\\";
        }
        pathNormalize();
        out.println("Directory switched to " + this.path);
    }
    private void pathNormalize(){
        this.path = this.path.replace("/", "\\");
        this.path = this.path.replaceAll("\\\\{2,}", "\\\\");
    }
    private boolean copy(String[] args)  {
        try {
            if (args.length <= 3) {
                Path from = Paths.get(this.path, args[1]);
                if (!Files.exists(from)) {
                    out.println("Nothing to copy");
                    return false;
                }
                Path to = Paths.get(args[2]);
                if (Files.exists(to)) {
                    out.println("Target file already exists");
                    return false;
                }
                Files.copy(from, to);
                return Files.exists(to);
            } else if (args[3].equals("-r")) {
                Path from = Paths.get(this.path, args[1]);
                Path to = Paths.get(args[2]);
                Files.createDirectory(to);
                copy(Objects.requireNonNull(from.toFile().listFiles()), args[2]);
                out.println(from);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    private void copy(File[] directory, String parent) throws IOException {
        for (File inner : directory){
            if (inner.isDirectory()){
                Files.createDirectory(Paths.get(parent+"/"+inner.toPath().getFileName()));
                copy(Objects.requireNonNull(inner.listFiles()), parent+"/"+inner.toPath().getFileName());
            } else {
                Files.copy(inner.toPath(), Paths.get(parent+"/"+inner.getName()));
            }
        }
    }
    private boolean rename(String[] args){
        Path from = Paths.get(this.path, args[1]);
        if (!from.toFile().exists()) {
            out.println("There is not such file, try another one");
            return false;
        }
        Path to = Paths.get(this.path, args[2]);
        if (to.toFile().exists()) {
            out.println("File already exists, can't rename to it.");
            return false;
        }
        return from.toFile().renameTo(to.toFile());
    }
    private boolean make(String[] args){
        boolean result = false;
        try {
            if ((args.length > 3 && args[3].equals("--anyway") || (args.length > 4 && args[4].equals("--anyway")))) {

                Files.deleteIfExists(Paths.get(this.path, args[2]));
            }
            switch (args[1]) {
                case "-d":
                    Path dir = Paths.get(this.path, args[2]);
                    if (Files.exists((dir))) {
                        out.println("Already exists!");
                        return false;
                    }
                    Files.createDirectory(dir);
                    if (Files.exists(dir)) result = true;
                    break;
                case "-f":
                    Path file = Paths.get(this.path, args[2]);
                    if (Files.exists((file))) {
                        out.println("Already exists!");
                        return false;
                    }
                    Files.createFile(file);
                    if (Files.exists(file)) result = true;
                    break;
                case "-l":
                    Path link = Paths.get(this.path, args[2]);
                    if (Files.exists((link))) {
                        out.println("Already exists!");
                        return false;
                    }
                    Files.createLink(link, Paths.get(args[3]));
                    if (Files.exists(link)) result = true;
                    break;
                case "-sl":
                    Path symbolicLink = Paths.get(this.path, args[2]);
                    if (Files.exists((symbolicLink))) {
                        out.println("Already exists!");
                        return false;
                    }
                    Files.createSymbolicLink(symbolicLink, Paths.get(args[3]));
                    if (Files.exists(symbolicLink)) result = true;
                    break;
                default:
                    out.println("Wrong option");
                    break;
            }
        }
        catch (IOException e) {
            out.println("Sorry, something went wrong!");
            }
        return result;
    }
    private boolean delete(String[] args){
        Path deleted = Paths.get(this.path + args[1]);
        try {
            Files.deleteIfExists(deleted);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return !deleted.toFile().exists();
    }
    private void list(){
        Path dir = Paths.get(this.path);
        ArrayList<File> content= new ArrayList<>();
        Collections.addAll(content, dir.toFile().listFiles());
        if (content.isEmpty()) {
            out.println("Directory is empty");
            return;
        }
        Collections.sort(content);
        content.forEach(file -> {
            try {
                System.out.println(file.getName() + " " + Files.size(file.toPath()) + " bytes " + (Files.isHidden(file.toPath()) ? "hidden " : "not hidden ") + (Files.isDirectory(file.toPath()) ? "directory" : ""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    private void list(String folder){
        Path dir = Paths.get(folder);
        if (!dir.toFile().isDirectory()) {
            out.println("There's not such directory");
            return;
        }
        ArrayList <File> content= new ArrayList<>();
        Collections.addAll(content, dir.toFile().listFiles());
        if (content.isEmpty()) {
            out.println("Directory is empty");
            return;
        }
        Collections.sort(content);
        content.forEach(file -> {
            try {
                System.out.println(file.getName() + " " + Files.size(file.toPath()) + " bytes " + Files.isHidden(file.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    private void open(String filePath){
        FileReader file;
        try {
            if (Files.notExists(Paths.get(filePath))||!Files.probeContentType(Paths.get(filePath)).contains("text/")){
                out.println("Can't read this file");
                return;
            }else if (Files.isDirectory(Paths.get(filePath))){
                changeDir(filePath);
                return;
            }
            file = new FileReader(filePath);
            int c ;
            while ((c = file.read()) != -1)
                out.print((char)c);
            System.out.println("\n");
        } catch (IOException e) {
            out.println("Can't read this file");
        }
    }

    private void archivation(String name){
        File newZip = new File(name + ".zip");
        FileOutputStream file;
        try {
            file = new FileOutputStream(newZip.toString());
            ZipOutputStream zipFile = new ZipOutputStream(file);
            ArrayList <File> files = new ArrayList<>();
            Collections.addAll(files, Paths.get(this.path).toFile().listFiles());
            System.out.println("In progress...");
            files.forEach(file1 -> {
                ZipEntry entry = new ZipEntry(file1.getName());
                try {
                    zipFile.putNextEntry(entry);
                    FileInputStream fis = new FileInputStream(file1.getAbsolutePath());
                    byte[] buf = new byte[fis.available()];
                    fis.read(buf);
                    zipFile.write(buf);
                    zipFile.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            zipFile.close();
            file.close();
            System.out.println("Done!");
        } catch (IOException e) {
            System.out.println("Can't create archive");
        }
    }

    private void fromArchive(FileInputStream zipFile, String pathTo){
        ZipInputStream unzip = new ZipInputStream(zipFile);
        try {
            ZipEntry entry;
            while ((entry=unzip.getNextEntry())!= null) {
                String name = entry.getName();
                FileOutputStream out = new FileOutputStream(pathTo + "\\" + name);
                int from;
                while ((from = unzip.read()) != -1){
                    out.write(from);
                }
                out.flush();
                unzip.closeEntry();
                out.close();
            }
            zipFile.close();
            System.out.println("Done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
