package gnu.trove.generate;

import java.io.*;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey
 * Date: 19.09.2004
 * Time: 12:26:43
 * To change this template use File | Settings | File Templates.
 */
public class Generate {
    private static final String[] WRAPPERS = new String[]{
            // v         V
            "double", "Double",
            "float", "Float",
            "int", "Integer",
            "long", "Long",
            "byte", "Byte",
    };

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: Generate <input directory> <output directory>");
            return;
        }
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        generate(inputDir, outputDir, "TPObjectHashMap.template", "src/gnu/trove/T", "ObjectHashMap.java");
        generate(inputDir, outputDir, "TObjectPHashMap.template", "src/gnu/trove/TObject", "HashMap.java");
        generate(inputDir, outputDir, "TArrayList.template", "src/gnu/trove/T", "ArrayList.java");
        generate(inputDir, outputDir, "THashingStrategy.template", "src/gnu/trove/T", "HashingStrategy.java");
        generate(inputDir, outputDir, "TPHash.template", "src/gnu/trove/T", "Hash.java");
        generate(inputDir, outputDir, "TPHashSet.template", "src/gnu/trove/T", "HashSet.java");
        generate(inputDir, outputDir, "TPIterator.template", "src/gnu/trove/T", "Iterator.java");
        generate(inputDir, outputDir, "TPFunction.template", "src/gnu/trove/T", "Function.java");
        generate(inputDir, outputDir, "TPProcedure.template", "src/gnu/trove/T", "Procedure.java");
        generate(inputDir, outputDir, "TObjectPIterator.template", "src/gnu/trove/TObject", "Iterator.java");
        generate(inputDir, outputDir, "TPObjectIterator.template", "src/gnu/trove/T", "ObjectIterator.java");
        generate(inputDir, outputDir, "TPObjectProcedure.template", "src/gnu/trove/T", "ObjectProcedure.java");
        generate(inputDir, outputDir, "TObjectPProcedure.template", "src/gnu/trove/TObject", "Procedure.java");

        generateKV(inputDir, outputDir, "TKVIterator.template", "src/gnu/trove/T", "Iterator.java");
        generateKV(inputDir, outputDir, "TKVProcedure.template", "src/gnu/trove/T", "Procedure.java");
        generateKV(inputDir, outputDir, "TKVHashMap.template", "src/gnu/trove/T", "HashMap.java");


        generateKV(inputDir, outputDir, "TPPMapDecorator.template", "src/gnu/trove/decorator/T", "HashMapDecorator.java");

        generate(inputDir, outputDir, "TPObjectMapDecorator.template", "src/gnu/trove/decorator/T", "ObjectHashMapDecorator.java");
        generate(inputDir, outputDir, "TObjectPMapDecorator.template", "src/gnu/trove/decorator/TObject", "HashMapDecorator.java");
        generate(inputDir, outputDir, "TPHashSetDecorator.template", "src/gnu/trove/decorator/T", "HashSetDecorator.java");
    }

    private static void generate(File inputDir, File outputDir, String templateName, String pathPrefix, String pathSuffix) throws IOException {
        String template = readFile(inputDir, templateName);
        for (int i = 0; i < WRAPPERS.length; i += 2) {
            String e = WRAPPERS[i];
            String ET = WRAPPERS[i + 1];
            String E = shortenInt(ET);
            String out = template;
            out = Pattern.compile("#e#").matcher(out).replaceAll(e);
            out = Pattern.compile("#E#").matcher(out).replaceAll(E);
            out = Pattern.compile("#ET#").matcher(out).replaceAll(ET);
            File outFile = new File(outputDir, pathPrefix + E + pathSuffix);
            writeFile(outFile, out);
        }
    }

    private static void generateKV(File inputDir, File outputDir, String templateName, String pathPrefix, String pathSuffix) throws IOException {
        String template = readFile(inputDir, templateName);
        for (int i = 0; i < WRAPPERS.length; i += 2) {
            for (int j = 0; j < WRAPPERS.length; j += 2) {
                String k = WRAPPERS[i];
                String KT = WRAPPERS[i + 1];
                String v = WRAPPERS[j];
                String VT = WRAPPERS[j + 1];
                String K = shortenInt(KT);
                String V = shortenInt(VT);
                String out = template;
                out = Pattern.compile("#v#").matcher(out).replaceAll(v);
                out = Pattern.compile("#V#").matcher(out).replaceAll(V);
                out = Pattern.compile("#k#").matcher(out).replaceAll(k);
                out = Pattern.compile("#K#").matcher(out).replaceAll(K);
                out = Pattern.compile("#KT#").matcher(out).replaceAll(KT);
                out = Pattern.compile("#VT#").matcher(out).replaceAll(VT);
                File outFile = new File(outputDir, pathPrefix + K + V + pathSuffix);
                writeFile(outFile, out);
            }
        }
    }

    private static void writeFile(File file, String out) throws IOException {
        file.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(file);
        writer.write(out);
        writer.close();
        System.out.println("File written: " + file);
    }

    private static String shortenInt(String type) {
        return type.equals("Integer") ? "Int" : type;
    }

    private static String readFile(File directory, String name) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(directory, name)));
        StringBuffer out = new StringBuffer();

        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            out.append(line);
            out.append("\n");
        }
        return out.toString();
    }
}
