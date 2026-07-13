package parma;

import java.util.Map;

public class AlertRule extends YamlAccess {

    public String getAlert() {
        return str("alert");
    }

    public void setAlert(String alert) {
        put("alert", alert);
    }

    public String getExpr() {
        return str("expr");
    }

    public void setExpr(String expr) {
        put("expr", expr);
    }

    public String getDurationFor() {
        return str("for");
    }

    public void setDurationFor(String durationFor) {
        put("for", durationFor);
    }

    public Map<String, String> getLabels() {
        return map("labels");
    }

    public void setLabels(Map<String, String> labels) {
        setMap("labels", labels);
    }

    public Map<String, String> getAnnotations() {
        return map("annotations");
    }

    public void setAnnotations(Map<String, String> annotations) {
        setMap("annotations", annotations);
    }
    
    @Override
    public String toString() {
        return "ALERT RULE: '" + getAlert() + "' | " + getExpr() + " | for " + getDurationFor() + " | " + getLabels().toString();
    }
}
