package ca.techgarage.bscm;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

public class ConfigManager {

    private static Yaml buildYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        return new Yaml(options);
    }

    public static void load(Class<?> configClass, Path configPath) {
        try {
            Map<String, Object> existing = Files.exists(configPath)
                    ? readFile(configPath)
                    : Collections.emptyMap();

            for (Field field : declaredStaticFields(configClass)) {
                Object raw = existing.get(field.getName());
                if (raw != null) applyValue(field, raw);
            }

            writeFile(configClass, configPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(Class<?> configClass, Path configPath)
            throws IOException, IllegalAccessException {
        Files.createDirectories(configPath.getParent());

        try (BufferedWriter writer = Files.newBufferedWriter(configPath)) {
            for (Field field : declaredStaticFields(configClass)) {
                Comment comment = field.getAnnotation(Comment.class);
                writer.write("# " + (comment != null ? comment.value() : field.getName()));
                writer.newLine();

                Yaml yaml = buildYaml();
                String valueLine = field.getName() + ": "
                        + yaml.dump(field.get(null)).stripTrailing();
                writer.write(valueLine);
                writer.newLine();
                writer.newLine();
            }
        }
    }

    public static void applyValue(Field field, Object raw) {
        try {
            Class<?> type = field.getType();

            if      (type == boolean.class && raw instanceof Boolean b)
                field.setBoolean(null, b);
            else if (type == int.class && raw instanceof Integer i)
                field.setInt(null, i);
            else if (type == int.class && raw instanceof Number n)
                field.setInt(null, n.intValue());
            else if (type == double.class && raw instanceof Number n)
                field.setDouble(null, n.doubleValue());
            else if (type == float.class && raw instanceof Number n)
                field.setFloat(null, n.floatValue());
            else if (type == long.class && raw instanceof Number n)
                field.setLong(null, n.longValue());
            else if (type == String.class && raw instanceof String s)
                field.set(null, s);
            else
                System.err.println("[BSCM] Type mismatch for '" + field.getName()
                        + "': got " + raw.getClass().getSimpleName()
                        + ", expected " + type.getSimpleName() + " — keeping default.");

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readFile(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            Yaml yaml = buildYaml();
            Map<String, Object> result = yaml.load(reader);
            return result != null ? result : Collections.emptyMap();
        }
    }

    public static List<Field> declaredStaticFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        for (Field f : clazz.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && !Modifier.isFinal(mod)) {
                f.setAccessible(true);
                result.add(f);
            }
        }
        return result;
    }
}