package ovh.equino.actracker.application.tagset;

import java.util.Set;
import java.util.UUID;

public record SearchTagSetsQuery(Integer pageSize,
                                 String pageId,
                                 String term,
                                 Set<UUID> excludeFilter) {
}