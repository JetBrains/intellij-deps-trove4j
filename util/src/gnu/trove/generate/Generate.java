package gnu.trove.generate;

import java.io.*;
import java.net.URL;
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
        generate("TPObjectHashMap.template", "src/gnu/trove/T", "ObjectHashMap.java");
        generate("TObjectPHashMap.template", "src/gnu/trove/TObject", "HashMap.java");
        generate("TArrayList.template", "src/gnu/trove/T", "ArrayList.java");
        generate("THashingStrategy.template", "src/gnu/trove/T", "HashingStrategy.java");
        generate("TPHash.template", "src/gnu/trove/T", "Hash.java");
        generate("TPHashSet.template", "src/gnu/trove/T", "HashSet.java");
        generate("TPIterator.template", "src/gnu/trove/T", "Iterator.java");
        generate("TPFunction.template", "src/gnu/trove/T", "Function.java");
        generate("TPProcedure.template", "src/gnu/trove/T", "Procedure.java");
        generate("TObjectPIterator.template", "src/gnu/trove/TObject", "Iterator.java");
        generate("TPObjectIterator.template", "src/gnu/trove/T", "ObjectIterator.java");
        generate("TPObjectProcedure.template", "src/gnu/trove/T", "ObjectProcedure.java");
        generate("TObjectPProcedure.template", "src/gnu/trove/TObject", "Procedure.java");

        generateKV("TKVIterator.template", "src/gnu/trove/T", "Iterator.java");
        generateKV("TKVProcedure.template", "src/gnu/trove/T", "Procedure.java");
        generateKV("TKVHashMap.template", "src/gnu/trove/T", "HashMap.java");


        generateKV("TPPMapDecorator.template", "src/gnu/trove/decorator/T", "HashMapDecorator.java");

        generate("TPObjectMapDecorator.template", "src/gnu/trove/decorator/T", "ObjectHashMapDecorator.java");
        generate("TObjectPMapDecorator.template", "src/gnu/trove/decorator/TObject", "HashMapDecorator.java");
        generate("TPHashSetDecorator.template", "src/gnu/trove/decorator/T", "HashSetDecorator.java");
    }

    private static void generate(String templateName, String pathPrefix, String pathSuffix) throws IOException {
        String template = readFile(templateName);
        for (int i = 0; i < WRAPPERS.length; i += 2) {
            String e = WRAPPERS[i];
            String ET = WRAPPERS[i + 1];
            String E = shortenInt(ET);
            String out = template;
            out = Pattern.compile("#e#").matcher(out).replaceAll(e);
            out = Pattern.compile("#E#").matcher(out).replaceAll(E);
            out = Pattern.compile("#ET#").matcher(out).replaceAll(ET);
            String outFile = pathPrefix + E + pathSuffix;
            writeFile(outFile, out);
        }
    }

    private static void generateKV(String templateName, String pathPrefix, String pathSuffix) throws IOException {
        String template = readFile(templateName);
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
                String outFile = pathPrefix + K + V + pathSuffix;
                writeFile(outFile, out);
            }
        }
    }

    private static void writeFile(String file, String out) throws IOException {
        File path = new File("generated/" + file).getAbsoluteFile();
        FileWriter writer = new FileWriter(path);
        writer.write(out);
        writer.close();
        System.out.println("File written: " + path);
    }

    private static String shortenInt(String type) {
        return type.equals("Integer") ? "Int" : type;
    }

    private static String readFile(String name) throws IOException {
        String packageName = Generate.class.getPackage().getName();
        URL resource = Generate.class.getClassLoader().getResource(packageName.replace('.', '/') + "/" + name);
        InputStream inputStream = resource.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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
