package org.example;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class Main {

    //  Remember to replace it with your own file address here

    public static final String PATH = "/home/tim/Desktop/";

    public static void main(String[] args) throws IOException {
        String jarPath = args[0];
        String jarPathOut = jarPath.split("\\.")[0] + "2.jar";

        String tempdir = jarPath.split("\\.")[0] + "/";
        String outputTempDir = jarPathOut.split("\\.")[0] + "/";

        ZipFile inputJar = new ZipFile(new File(jarPath));
        ZipFile outPutJar = new ZipFile(new File(jarPathOut));


        inputJar.extractAll(tempdir);

        File tempdirFolder = new File(tempdir);
        File outPutTempDirFolder = new File(outputTempDir);
        outPutTempDirFolder.mkdir();

        //Create Folder structure in the outPutTempDir
        Files.walk(Paths.get(tempdirFolder.getPath()))
                .filter(n -> n.toFile().isDirectory())
                .forEach(n -> {
                    String relativePath = new File(tempdir).toURI().relativize(n.toFile().toURI()).getPath();
                    System.out.println("Found Folder: " + relativePath);
                    new File(outPutTempDirFolder, relativePath).mkdirs();
                });


        //Transform the files
        Files.walk(Paths.get(tempdirFolder.getPath()))
                .filter(Files::isRegularFile)
                .filter(n -> n.toFile().getName().endsWith(".class"))
                .forEach(n -> {
                    System.out.println(n);
                    try {
                        parseClass(n.toFile(), outputTempDir, tempdir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        //Add all the non class files to the zip
        Files.walk(Paths.get(tempdirFolder.getPath()))
                .filter(Files::isRegularFile)
                .filter(n -> !n.toFile().getName().endsWith(".class"))
                .forEach(n -> {
                    try {
                        String relativePath = new File(tempdir).toURI().relativize(n.toFile().toURI()).getPath();
                        Files.copy(n, Path.of(outPutTempDirFolder.toPath() + File.separator + relativePath));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        ZipParameters parameters = new ZipParameters();
        parameters.setIncludeRootFolder(false);
        try {
            outPutJar.createSplitZipFileFromFolder(outPutTempDirFolder, parameters, false, 65536);
        } catch (ZipException e) {
            throw new RuntimeException(e);
        }



//
//        //Add all the transformed class files
//        Files.walk(Paths.get(outPutTempDirFolder.getPath()))
//                .filter(Files::isRegularFile)
//                .filter(n -> n.toFile().getName().endsWith(".class"))
//                .forEach(n -> {
//                    System.out.println("Adding: " + n.toFile().toString());
//                });


    }


    private static void parseClass(File file, String outputPath, String inputPath) throws IOException {


        String relativePath = new File(inputPath).toURI().relativize(file.toURI()).getPath();
        System.out.println(relativePath);

        ClassReader reader = new ClassReader(new FileInputStream(file));
        ClassWriter writer = new ClassWriter(reader, 0);

        TraceClassVisitor printer = new TraceClassVisitor(writer,
                new PrintWriter(System.getProperty("java.io.tmpdir")
                        + File.separator + "TheClass" + ".log"));

        ClassAdapter adapter = new ClassAdapter(printer);
        reader.accept(adapter, 0);
        byte[] b = writer.toByteArray();

        Files.write(new File(outputPath + relativePath).toPath(), b);


    }

}