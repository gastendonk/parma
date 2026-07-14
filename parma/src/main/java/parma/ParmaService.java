package parma;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.git.Repository;
import github.soltaufintel.amalia.git.RepositoryDefinition;
import github.soltaufintel.amalia.rest.REST;
import parma.Silence.Matcher;
import parma.Silence.SilenceCreated;

/**
 * Prometheus AleRtManager Api
 */
public class ParmaService {
    private static final String ENDPOINT1 = "/api/v2/silence/";
    private static final String ENDPOINT2 = "/api/v2/silences";
    private static final String ENDPOINT3 = "/api/v1/query";
    private final ParmaConfig config;

    public ParmaService(ParmaConfig config) {
        this.config = config;
    }

    // Ich geh erstmal davon aus, dass ich die alertmanager-Datei nur von Hand bearbeite.
    // Die prometheus.yml muss ich nur lesen.
    // Die alert-rules yml files lesen und schreiben.
    
    public AlertRulesFiles loadAlertRulesFiles() throws IOException {
        // Git part ----
        config.getRepository().getLocalFolder().getParentFile().mkdirs();
        var repo = new Repository(config.getRepository());
        repo.pull(false);
        var id = repo.getCurrentCommitHash();
        var folder1 = config.getRepository().getLocalFolder();
        
        // File part ----
        AlertRulesFiles ret = new AlertRulesFiles();
        ret.setCommitHash(id);
        var folder2 = new File(folder1, config.getFolder());
        var prometheusYaml = new PrometheusFile(folder2);
        var reader = new AlertRulesFileReader();
        for (String arf : prometheusYaml.getRuleFiles()) {
            ret.add(reader.read(new File(folder2, arf)));
        }
        return ret;
    }
    
    public void saveAlertRuleFiles(AlertRulesFiles files) {
        RepositoryDefinition rd = config.getRepository();
        rd.getLocalFolder().getParentFile().mkdirs();
        var repo = new Repository(rd);
        repo.pull(false);
        if (files.getCommitHash() != null && files.getCommitHash().equals(repo.getCurrentCommitHash())) {
            throw new RuntimeException("Concurrent modification of alert rules files!");
        }
        var folder1 = rd.getLocalFolder();
        
        var folder2 = new File(folder1, config.getFolder());
        for (AlertRulesFile file : files) {
            FileService.savePlainTextFile(new File(folder2, file.getName()), file.yaml());
        }
        
        repo.commit("update alert rules", rd.getUser(), config.getMailAddress(), rd.getUser(), rd.getPassword());
        files.setCommitHash(repo.getCurrentCommitHash());
    }

    public List<Silence> getSilences() {
        var rest = new REST(config.getAlertmanagerHost() + ENDPOINT2).get();
        var json = rest.response();
        rest.close();
        Type silencesType = new TypeToken<List<Silence>>() {
        }.getType();
        return Silence.gson().fromJson(json, silencesType);
    }

    public String createSilence(String title, String comment, long hours, List<Matcher> matchers) {
        return createSilence(title, comment, null, OffsetDateTime.now().plusHours(hours), matchers);
    }

    public String createSilence(String title, String comment, OffsetDateTime startsAt, OffsetDateTime endsAt,
            List<Matcher> matchers) {
        Silence silence = new Silence();
        silence.setCreatedBy(title);
        silence.setComment(comment);
        silence.setStartsAt(startsAt == null ? OffsetDateTime.now() : startsAt);
        silence.setEndsAt(endsAt);
        silence.setMatchers(matchers);
        return createSilence(silence);
    }

    public String createSilence(Silence silence) {
        var json = Silence.gson().toJson(silence);
        return new REST(config.getAlertmanagerHost() + ENDPOINT2).post(json, REST.json_cp1252())
                .fromJson(SilenceCreated.class).getSilenceID();
    }

    /**
     * Delete silence
     * @param id silence ID
     */
    public void expireSilence(String id) {
        new REST(config.getAlertmanagerHost() + ENDPOINT1 + id).delete().close();
    }
    
    public void reloadPrometheus() {
        REST.post(config.getPrometheusHost() + "/-/reload", "");
    }
    
    public void reloadAlertmanager() {
        REST.post(config.getAlertmanagerHost() + "/-/reload", "");
    }
    
    public List<PrometheusResult> queryAlerts() {
        return new REST(config.getPrometheusHost() + ENDPOINT3 + "?query=ALERTS")
                .get()
                .fromJson(PrometheusResponse.class)
                .data()
                .result();
    }
}
