package org.example;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.example.io.ByteProvider;
import org.example.transformer.LocalVariableReplacer;
import org.example.transformer.MethodRenamer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Collectors;


public class Main {

    //  Remember to replace it with your own file address here

    public static ByteProvider byteProvider = new ByteProvider();

    public static void main(String[] args) throws IOException {
        String jarPath = "/home/tim/Desktop/ByteCodeTest.jar";
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

        Obfuscator.addTransformer(new MethodRenamer());
        Obfuscator.addTransformer(new LocalVariableReplacer(LocalVariableReplacer.NamingMode.UNDERSCORE));

        //Transform the files
        HashMap<File, byte[]> classes = new HashMap<>();
        Files.walk(Paths.get(tempdirFolder.getPath()))
                .filter(Files::isRegularFile)
                .filter(n -> n.toFile().getName().endsWith(".class"))
                .forEach(n -> {
                    System.out.println(n);
                    try {
                        classes.put(n.toFile(), Files.readAllBytes(n));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        parseClasses(classes, outputTempDir, tempdir);

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


    private static void parseClasses(HashMap<File, byte[]> classes, String outputPath, String inputPath) throws IOException {

        HashMap<File, byte[]> classesOut = Obfuscator.transform(classes);

        classesOut.forEach( (file, bytes) -> {
            String relativePath = new File(inputPath).toURI().relativize(file.toURI()).getPath();
            byteProvider.classBytes.put(relativePath.split("\\.")[0], bytes);
            try {
                Files.write(new File(outputPath + relativePath + "//").toPath(), bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}