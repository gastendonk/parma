package parma;

import java.util.ArrayList;
import java.util.List;

public class AlertGroup extends YamlAccess {
    private List<AlertRule> rules = new ArrayList<>();

    public String getName() {
        return str("name");
    }

    public void setName(String name) {
        put("name", name);
    }

    public List<AlertRule> getRules() {
        return rules;
    }
    
    public void setRules(List<AlertRule> rules) {
        this.rules = rules;
    }

    public void add(AlertRule rule) {
        rules.add(rule);
        getYaml().put("rules", rules);
    }
}
