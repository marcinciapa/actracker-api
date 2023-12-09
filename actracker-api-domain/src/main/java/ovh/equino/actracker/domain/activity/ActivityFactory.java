package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

import static java.lang.Boolean.TRUE;

public final class ActivityFactory {

    private static final Boolean DELETED = TRUE;

    private final ActivityDataSource activityDataSource;
    private final TagDataSource tagDataSource;

    ActivityFactory(ActivityDataSource activityDataSource, TagDataSource tagDataSource) {
        this.activityDataSource = activityDataSource;
        this.tagDataSource = tagDataSource;
    }

    public Activity create(User creator,
                           String title,
                           Instant startTime,
                           Instant endTime,
                           String comment,
                           Collection<TagId> tags,
                           Collection<MetricValue> metricValues) {

        var activitiesAccessibilityVerifier = new ActivitiesAccessibilityVerifier(activityDataSource, creator);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource, creator);
        var metricsAccessibilityVerifier = new MetricsAccessibilityVerifier(tagDataSource, creator);
        var validator = new ActivityValidator(tagsAccessibilityVerifier, metricsAccessibilityVerifier);

        var activity = new Activity(
                new ActivityId(),
                creator,
                title,
                startTime,
                endTime,
                comment,
                Collections.emptyList(),
                Collections.emptyList(),
                !DELETED,
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                validator
        );
        activity.validate();
        return activity;
    }

    // TODO should be user in context, not creator!!!
    public Activity reconstitute(ActivityId id,
                                 User creator,
                                 String title,
                                 Instant startTime,
                                 Instant endTime,
                                 String comment,
                                 Collection<TagId> tags,
                                 Collection<MetricValue> metricValues,
                                 boolean deleted) {

        var activitiesAccessibilityVerifier = new ActivitiesAccessibilityVerifier(activityDataSource, creator);
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource, creator);
        var metricsAccessibilityVerifier = new MetricsAccessibilityVerifier(tagDataSource, creator);
        var validator = new ActivityValidator(tagsAccessibilityVerifier, metricsAccessibilityVerifier);

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
                activitiesAccessibilityVerifier,
                tagsAccessibilityVerifier,
                metricsAccessibilityVerifier,
                validator
        );
    }
}
