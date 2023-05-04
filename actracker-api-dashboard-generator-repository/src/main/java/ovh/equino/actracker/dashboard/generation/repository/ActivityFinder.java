package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.domain.dashboard.DashboardGenerationCriteria;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;

final class ActivityFinder {

    private static final Integer PAGE_SIZE = 500;

    private final ActivitySearchEngine searchEngine;

    ActivityFinder(ActivitySearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    List<ActivityDto> find(DashboardGenerationCriteria generationCriteria) {
        List<ActivityDto> activities = new ArrayList<>();
        String pageId = "";
        while (pageId != null) {
            EntitySearchResult<ActivityDto> searchResult = fetchNextPageOfActivities(generationCriteria, pageId);
            pageId = searchResult.nextPageId();
            activities.addAll(alignedToTimeRange(searchResult.results(), generationCriteria));
        }
        return activities;
    }

    private EntitySearchResult<ActivityDto> fetchNextPageOfActivities(DashboardGenerationCriteria generationCriteria,
                                                                      String pageId) {

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                generationCriteria.generator(),
                PAGE_SIZE,
                pageId,
                null,
                generationCriteria.timeRangeStart(),
                generationCriteria.timeRangeEnd(),
                null,
                EntitySortCriteria.irrelevant()
        );
        return searchEngine.findActivities(searchCriteria);
    }

    private Collection<ActivityDto> alignedToTimeRange(List<ActivityDto> activities, DashboardGenerationCriteria generationCriteria) {
        return activities.stream()
                .map(activity ->
                        new ActivityDto(
                                activity.title(),
                                latestOf(generationCriteria.timeRangeStart(), activity.startTime()),
                                earliestOf(generationCriteria.timeRangeEnd(), activity.endTime()),
                                activity.comment(),
                                activity.tags()
                        )
                )
                .filter(activity -> nonNull(activity.startTime()) && nonNull(activity.endTime()))
                .toList();
    }

    Instant earliestOf(Instant... candidates) {
        return stream(candidates)
                .filter(Objects::nonNull)
                .min(Instant::compareTo)
                .orElse(null);
    }

    Instant latestOf(Instant... candidates) {
        return stream(candidates)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);
    }
}
