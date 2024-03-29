package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNullElse;

class ActivityFactoryImpl implements ActivityFactory {

    private static final Boolean DELETED = TRUE;

    private final ActorExtractor actorExtractor;
    private final ActivitiesAccessibilityVerifier activitiesAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final MetricsAccessibilityVerifier metricsAccessibilityVerifier;

    ActivityFactoryImpl(ActorExtractor actorExtractor,
                        ActivitiesAccessibilityVerifier activitiesAccessibilityVerifier,
                        TagsAccessibilityVerifier tagsAccessibilityVerifier,
                        MetricsAccessibilityVerifier metricsAccessibilityVerifier) {

        this.actorExtractor = actorExtractor;
        this.activitiesAccessibilityVerifier = activitiesAccessibilityVerifier;
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.metricsAccessibilityVerifier = metricsAccessibilityVerifier;
    }

    @Override
    public Activity create(String title,
                           Instant startTime,
                           Instant endTime,
                           String comment,
                           Collection<TagId> tags,
                           Collection<MetricValue> metricValues) {

        var creator = actorExtractor.getActor();
        var validator = new ActivityValidator();

        var nonNullTags = requireNonNullElse(tags, new ArrayList<TagId>());
        var nonNullMetricValues = requireNonNullElse(metricValues, new ArrayList<MetricValue>());
        validateTagsAccessibleFor(creator, nonNullTags);
        validateMetricsAccessibleFor(creator, nonNullMetricValues, nonNullTags);

        var activity = new Activity(
                new ActivityId(),
                creator,
                title,
                startTime,
                endTime,
                comment,
                nonNullTags,
                nonNullMetricValues,
                !DELETED,
                actorExtractor,
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                validator
        );
        activity.validate();
        return activity;
    }

    @Override
    public Activity reconstitute(ActivityId id,
                                 User creator,
                                 String title,
                                 Instant startTime,
                                 Instant endTime,
                                 String comment,
                                 Collection<TagId> tags,
                                 Collection<MetricValue> metricValues,
                                 boolean deleted) {

        var validator = new ActivityValidator();

        return new Activity(
                id,
                creator,
                title,
                startTime,
                endTime,
                comment,
                tags,
                metricValues,
                deleted,
                actorExtractor,
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                validator
        );
    }

    private void validateTagsAccessibleFor(User user, Collection<TagId> tags) {
        tagsAccessibilityVerifier.nonAccessibleFor(user, tags)
                .stream()
                .findFirst()
                .ifPresent((inaccessibleTag) -> {
                    String errorMessage = "Tag with ID %s not found".formatted(inaccessibleTag.id());
                    throw new EntityInvalidException(Activity.class, errorMessage);
                });
    }

    private void validateMetricsAccessibleFor(User user, Collection<MetricValue> metricValues, Collection<TagId> tags) {

        var setMetrics = metricValues
                .stream()
                .map(MetricValue::metricId)
                .map(MetricId::new)
                .toList();
        metricsAccessibilityVerifier.nonAccessibleFor(user, setMetrics, tags)
                .stream()
                .findFirst()
                .ifPresent((inaccessibleMetric) -> {
                    String errorMessage = "Metric with ID %s not found".formatted(inaccessibleMetric.id());
                    throw new EntityInvalidException(Activity.class, errorMessage);
                });
    }
}
