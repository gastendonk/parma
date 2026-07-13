package parma;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import github.soltaufintel.amalia.base.FileService;

public class AlertRulesFileTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void read_and_write_alertRulesFile() throws IOException {
        // Prepare
        File tempFile = tempFolder.newFile("read_and_write_alertRulesFile.yml");
        FileService.savePlainTextFile(tempFile, content());

        // Read test
        AlertRulesFile file = new AlertRulesFileReader().read(tempFile);

        for (AlertGroup group : file.getGroups()) {
            Assert.assertEquals("infrastruktur_alarme", group.getName());
            Assert.assertEquals(1, group.getRules().size());
            for (AlertRule rule : group.getRules()) {
                Assert.assertEquals("InstanzDown6", rule.getAlert());
                rule.setAlert("neuer_Alarm");
                rule.getLabels().put("foo", "bar");
            }
        }
        
        // Write test
        Assert.assertEquals(expectation(), file.yaml());
    }

    private String content() {
        return """
                groups:
                  - name: infrastruktur_alarme
                    rules:
                      - alert: InstanzDown6
                        expr: up == 0
                        for: 15s  # Muss 1 Minute lang down sein, bevor der Alarm feuert
                        labels:
                          kat: "K-1"
                        annotations:
                          summary: "Instanz {{ $labels.instance }} ist nicht erreichbar! Das ist ein Test."
                          description: "Das Target {{ $labels.instance }} antwortet seit mehr als 1 Minute nicht auf Scrapes."
                                """;
    }
    
    private String expectation() {
        return """
                groups:
                - name: infrastruktur_alarme
                  rules:
                  - alert: neuer_Alarm
                    annotations:
                      description: Das Target {{ $labels.instance }} antwortet seit mehr als 1 Minute
                        nicht auf Scrapes.
                      summary: Instanz {{ $labels.instance }} ist nicht erreichbar! Das ist ein Test.
                    expr: up == 0
                    for: 15s
                    labels:
                      foo: bar
                      kat: K-1
                              """;
    }

    @Test
    public void createAlertRule() throws IOException {
        // Prepare
        String content = """
                groups:
                  - name: infrastruktur_alarme
                    rules:
                                """;
        File tempFile = tempFolder.newFile("createAlertRule.yml");
        FileService.savePlainTextFile(tempFile, content);
        AlertRulesFile file = new AlertRulesFileReader().read(tempFile);
        AlertGroup group = file.getGroups().get(0);

        // Test
        addRule(group);
        
        // Verify
        Assert.assertEquals(expectation2(), file.yaml());
    }
    
    private AlertRule addRule(AlertGroup group) {
        AlertRule rule = new AlertRule();
        rule.setAlert("neuer_Alarm");
        rule.setExpr("up == 0");
        rule.setDurationFor("15s");
        rule.setLabels(Map.of("kat", "K-1", "foo", "bar"));
        rule.setAnnotations(Map.of("summary", "Instanz {{ $labels.instance }} ist nicht erreichbar! Das ist ein Test.", //
                "description", "Das Target {{ $labels.instance }} antwortet seit mehr als 1 Minute nicht auf Scrapes."));
        group.add(rule);
        return rule;
    }

    @Test
    public void createAlertRule_fromScratch() throws IOException {
        test3();
    }

    private AlertRulesFile test3() {
        AlertRulesFile file = new AlertRulesFile();
        AlertGroup group = file.add("infrastruktur_alarme");
        addRule(group);
        Assert.assertEquals(1, group.getRules().size());
        Assert.assertEquals(expectation2(), file.yaml());
        return file;
    }
    
    @Test
    public void deleteAlertRule() throws IOException {
        // Prepare
        AlertRulesFile file = test3();
        
        // Test
        file.getGroups().get(0).getRules().remove(0);
        
        // Verify
        Assert.assertEquals("""
                groups:
                - name: infrastruktur_alarme
                  rules: [
                    ]
                """, file.yaml());
    }

    private String expectation2() {
        return """
                groups:
                - name: infrastruktur_alarme
                  rules:
                  - alert: neuer_Alarm
                    annotations:
                      description: Das Target {{ $labels.instance }} antwortet seit mehr als 1 Minute
                        nicht auf Scrapes.
                      summary: Instanz {{ $labels.instance }} ist nicht erreichbar! Das ist ein Test.
                    expr: up == 0
                    for: 15s
                    labels:
                      foo: bar
                      kat: K-1
                              """;
    }

    @Test
    public void twoGroups() {
        // Prepare
        AlertRulesFile file = new AlertRulesFile();
        AlertGroup group = file.add("infrastruktur_alarme");
        addRule(group);
        
        // Test: add 2nd group
        group = file.add("group_2");
        var rule = addRule(group);
        rule.setAlert("was anderes");
        rule.setAnnotations(new HashMap<>());
        
        // Verify
        Assert.assertEquals("""
            groups:
            - name: infrastruktur_alarme
              rules:
              - alert: neuer_Alarm
                annotations:
                  description: Das Target {{ $labels.instance }} antwortet seit mehr als 1 Minute
                    nicht auf Scrapes.
                  summary: Instanz {{ $labels.instance }} ist nicht erreichbar! Das ist ein Test.
                expr: up == 0
                for: 15s
                labels:
                  foo: bar
                  kat: K-1
            - name: group_2
              rules:
              - alert: was anderes
                annotations: {
                  }
                expr: up == 0
                for: 15s
                labels:
                  foo: bar
                  kat: K-1
                """, file.yaml());
    }
}
