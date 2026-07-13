package parma;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PrometheusFile extends YamlAccess {

    public PrometheusFile(File folder) throws IOException {
        var file = new File(folder, "prometheus.yml");
        if (file.isFile()) {
            load(file);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRuleFiles() {
        return (List<String>) getYaml().get("rule_files");
    }
}
