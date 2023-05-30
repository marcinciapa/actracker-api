package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.dashboard.ChartBucketData;
import ovh.equino.actracker.domain.dashboard.DashboardChartData;
import ovh.equino.actracker.domain.tag.TagId;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static ovh.equino.actracker.domain.dashboard.ChartBucketData.Type.TAG;

class TagChartGenerator extends ChartGenerator {

    private final PercentageCalculator percentageCalculator = new PercentageCalculator();

    TagChartGenerator(String chartName, Set<TagId> allowedTags, Set<TagId> availableTags) {
        super(chartName, allowedTags, availableTags);
    }

    @Override
    DashboardChartData generate(Collection<ActivityDto> activities) {
        Map<TagId, Duration> durationByTag = tags.stream()
                .collect(toMap(
                        identity(),
                        tag -> totalDurationOf(activitiesWithTag(tag, activities))
                ));

        return new DashboardChartData(chartName, toBuckets(durationByTag));
    }

    Duration totalDurationOf(Collection<ActivityDto> activities) {
        return activities.stream()
                .map(activity -> Duration.between(activity.startTime(), activity.endTime()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    Collection<ActivityDto> activitiesWithTag(TagId tag, Collection<ActivityDto> activities) {
        return activities.stream()
                .filter(activityDto -> activityDto.tags().contains(tag.id()))
                .toList();
    }

    private List<ChartBucketData> toBuckets(Map<TagId, Duration> durationByTag) {
        Duration totalMeasuredDuration = durationByTag.values().stream()
                .reduce(Duration.ZERO, Duration::plus);

        return durationByTag.entrySet().stream()
                .map(entry -> toBucket(entry, totalMeasuredDuration))
                .toList();
    }

    private ChartBucketData toBucket(Map.Entry<TagId, Duration> tagWithDuration, Duration totalMeasuredDuration) {
        TagId tag = tagWithDuration.getKey();
        BigDecimal tagDuration = BigDecimal.valueOf(tagWithDuration.getValue().toSeconds());
        BigDecimal totalDuration = BigDecimal.valueOf(totalMeasuredDuration.toSeconds());
        return new ChartBucketData(
                tag.id().toString(),
                null,
                null,
                TAG,
                percentageCalculator.percentage(tagDuration, totalDuration),
                percentageCalculator.percentage(tagDuration, totalDuration),
                null
        );
    }
}
