package ovh.equino.actracker.domain.tag;

import java.util.Collection;
import java.util.UUID;

public record TagDto(

        UUID id,
        UUID creatorId,
        String name,
        Collection<MetricDto> metrics,
        boolean deleted

) {

    // Constructor for data provided from input
    public TagDto(String name, Collection<MetricDto> metrics) {
        this(null, null, name, metrics, false);
    }

}
