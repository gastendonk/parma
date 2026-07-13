package parma;

import java.util.ArrayList;
import java.util.List;

public class AlertRulesFile extends YamlAccess {
    /** filename */
    private String name;
    private List<AlertGroup> groups = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AlertGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<AlertGroup> groups) {
        this.groups = groups;
    }

    public AlertGroup add(String name) {
        AlertGroup group = new AlertGroup();
        group.setName(name);
        groups.add(group);
        getYaml().put("groups", groups);
        return group;
    }
}
