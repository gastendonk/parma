package parma;

import java.util.List;

public record PrometheusData(String resultType, List<PrometheusResult> result) {
}
