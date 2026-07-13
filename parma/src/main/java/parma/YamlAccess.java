package parma;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public abstract class YamlAccess {
    private Map<String, Object> yaml = new HashMap<>();
    
    public Map<String, Object> getYaml() {
        return yaml;
    }

    public void setYaml(Map<String, Object> yaml) {
        this.yaml = yaml;
    }
    
    protected String str(String name) {
        return (String) yaml.get(name);
    }
    
    protected void put(String key, Object value) {
        yaml.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, String> map(String key) {
        if (yaml.containsKey(key)) {
            return (Map<String, String>) yaml.get(key);
        }
        return new HashMap<>();
    }
    
    protected void setMap(String key, Map<String, String> map) {
        yaml.put(key, map);
    }
    
    public String yaml() {
        if (yaml == null) {
            return "";
        }

        // 1. Die gesamte Struktur rekursiv bereinigen und strikt alphabetisch sortieren (TreeMaps)
        Object cleanedStructure = cleanAndSort(yaml);

        // 2. YAML-Optionen fuer schoenes Blockformat setzen
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        Representer representer = new Representer(options);
        // Verhindert, dass SnakeYAML "!!java.util.TreeMap" in dein YAML schreibt
        representer.addClassTag(TreeMap.class, Tag.MAP);

        return new Yaml(representer, options).dump(cleanedStructure);
    }
    
    @SuppressWarnings("unchecked")
    private Object cleanAndSort(Object data) {
        if (data == null) {
            return null;
        }

        // Fall 1: Es ist eine Map (oder deine Root-Map)
        if (data instanceof Map) {
            Map<String, Object> srcMap = (Map<String, Object>) data;
            Map<String, Object> sortedMap = new TreeMap<>();
            
            for (Map.Entry<String, Object> entry : srcMap.entrySet()) {
                // Ignoriere das stoerende interne yaml-Feld ueberall
                if ("yaml".equals(entry.getKey())) {
                    continue;
                }
                sortedMap.put(entry.getKey(), cleanAndSort(entry.getValue()));
            }
            return sortedMap;
        }

        // Fall 2: Es ist eine Liste (wie unter "groups" oder "rules")
        if (data instanceof List) {
            List<Object> srcList = (List<Object>) data;
            List<Object> cleanedList = new ArrayList<>();
            for (Object item : srcList) {
                cleanedList.add(cleanAndSort(item));
            }
            return cleanedList;
        }

        // Fall 3: Es ist dein echtes Java-Objekt (z.B. parma.AlertRule)
        // Wir nutzen hier Reflection, um das POJO in eine TreeMap zu zerlegen
        if (data.getClass().getName().startsWith("parma.") || data.getClass().getSimpleName().contains("Alert")) {
            Map<String, Object> sortedMap = new TreeMap<>();
            try {
                // Holt alle Getter/Properties ueber SnakeYAMLs eigenen Mechanismus
                PropertyUtils utils = new PropertyUtils();
                for (Property prop : utils.getProperties(data.getClass())) {
                    String name = prop.getName();
                    
                    // Ignoriere das Feld "yaml" und das standardmaessige Class-Property von Java
                    if ("yaml".equals(name) || "class".equals(name)) {
                        continue;
                    }
                    
                    Object value = prop.get(data);
                    
                    // Prometheus erwartet im YAML "for", dein POJO nutzt aber "durationFor"
                    String yamlKey = "durationFor".equals(name) ? "for" : name;
                    
                    sortedMap.put(yamlKey, cleanAndSort(value));
                }
            } catch (Exception e) {
                // Fallback, falls Reflection fehlschlaegt
                return data.toString();
            }
            return sortedMap;
        }

        // Fall 4: Primitiver Datentyp (String, Integer, Boolean) -> Einfach zurueckgeben
        return data;
    }
    
    public void load(File file) throws IOException {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            setYaml(new Yaml().load(in));
        }
    }
}
