package parma;

import java.util.ArrayList;

public class AlertRulesFiles extends ArrayList<AlertRulesFile> {
    private String commitHash;
    
    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }
}
