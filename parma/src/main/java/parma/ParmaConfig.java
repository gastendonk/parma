package parma;

import github.soltaufintel.amalia.git.RepositoryDefinition;

public interface ParmaConfig {

    /**
     * @return e.g. "http://grafana:9093"
     */
    String getAlertmanagerHost();
    
    /**
     * @return Git repository of GitOps/alert files
     */
    RepositoryDefinition getRepository();
    
    /**
     * @return mail address for committing (Git)
     */
    String getMailAddress();
    
    /**
     * @return relative folder to prometheus.yml and alert rules files
     */
    String getFolder();
}
