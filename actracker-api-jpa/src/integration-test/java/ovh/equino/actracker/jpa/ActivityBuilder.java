package ovh.equino.actracker.jpa;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.MetricValue;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static ovh.equino.actracker.jpa.TestUtil.*;

public final class ActivityBuilder {

    private ActivityDto newActivity;

    ActivityBuilder(TenantDto creator) {
        this.newActivity = new ActivityDto(
                nextUUID(),
                creator.id(),
                randomString(),
                null,
                null,
                randomString(),
                Set.of(nextUUID(), nextUUID(), nextUUID()),
                List.of(
                        new MetricValue(nextUUID(), randomBigDecimal()),
                        new MetricValue(nextUUID(), randomBigDecimal()),
                        new MetricValue(nextUUID(), randomBigDecimal())
                ),
                false
        );
    }

    public ActivityBuilder named(String name) {
        this.newActivity = new ActivityDto(
                newActivity.id(),
                newActivity.creatorId(),
                name,
                newActivity.startTime(),
                newActivity.endTime(),
                newActivity.comment(),
                newActivity.tags(),
                newActivity.metricValues(),
                newActivity.deleted()
        );
        return this;
    }

    public ActivityBuilder startedAt(long epochSeconds) {
        this.newActivity = new ActivityDto(
                newActivity.id(),
                newActivity.creatorId(),
                newActivity.title(),
                Instant.ofEpochSecond(epochSeconds),
                newActivity.endTime(),
                newActivity.comment(),
                newActivity.tags(),
                newActivity.metricValues(),
                newActivity.deleted()
        );
        return this;
    }

    public ActivityBuilder finishedAt(long epochSeconds) {
        this.newActivity = new ActivityDto(
                newActivity.id(),
                newActivity.creatorId(),
                newActivity.title(),
                newActivity.startTime(),
                Instant.ofEpochSecond(epochSeconds),
                newActivity.comment(),
                newActivity.tags(),
                newActivity.metricValues(),
                newActivity.deleted()
        );
        return this;
    }

    public ActivityBuilder deleted() {
        this.newActivity = new ActivityDto(
                newActivity.id(),
                newActivity.creatorId(),
                newActivity.title(),
                newActivity.startTime(),
                newActivity.endTime(),
                newActivity.comment(),
                newActivity.tags(),
                newActivity.metricValues(),
                true
        );
        return this;
    }

    public ActivityBuilder withTags(TagDto... tags) {
        this.newActivity = new ActivityDto(
                newActivity.id(),
                newActivity.creatorId(),
                newActivity.title(),
                newActivity.startTime(),
                newActivity.endTime(),
                newActivity.comment(),
                stream(tags).map(TagDto::id).collect(toUnmodifiableSet()),
                newActivity.metricValues(),
                newActivity.deleted()
        );
        return this;
    }

    public ActivityBuilder withMetricValues(MetricValue... values) {
        this.newActivity = new ActivityDto(
                newActivity.id(),
                newActivity.creatorId(),
                newActivity.title(),
                newActivity.startTime(),
                newActivity.endTime(),
                newActivity.comment(),
                newActivity.tags(),
                stream(values).toList(),
                newActivity.deleted()
        );
        return this;
    }

    public ActivityDto build() {
        return newActivity;
    }
}
