package ovh.equino.actracker.domain.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.tag.MetricId;
import ovh.equino.actracker.domain.tag.MetricsExistenceVerifier;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsExistenceVerifier;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.List;

import static java.math.BigDecimal.TEN;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityValidatorTest {

    private static final User CREATOR = new User(randomUUID());
    private static final String ACTIVITY_TITLE = "activity name";
    private static final Instant EMPTY_START_TIME = null;
    private static final Instant EMPTY_END_TIME = null;
    private static final String EMPTY_COMMENT = null;

    private static final String VALIDATION_ERROR = "Activity invalid: %s";
    private static final String END_BEFORE_START_ERROR = "End time is before start time";
    private static final String NOT_EXISTING_TAGS_ERROR = "Selected tags do not exist: %s";
    private static final String NOT_EXISTING_METRICS_ERROR = "Selected metrics do not exist in selected tags: %s";
    private static final List<TagId> EMPTY_TAGS = emptyList();
    private static final List<MetricValue> EMPTY_METRIC_VALUES = emptyList();
    private static final boolean DELETED = true;

    @Mock
    private TagsExistenceVerifier tagsExistenceVerifier;
    @Mock
    private MetricsExistenceVerifier metricsExistenceVerifier;

    private ActivityValidator validator;

    @BeforeEach
    void setUp() {
        this.validator = new ActivityValidator(tagsExistenceVerifier, metricsExistenceVerifier);
    }

    @Test
    void shouldNotFailWhenActivityValid() {
        // given
        Activity activity = new Activity(
                new ActivityId(),
                CREATOR,
                ACTIVITY_TITLE,
                EMPTY_START_TIME,
                EMPTY_END_TIME,
                EMPTY_COMMENT,
                EMPTY_TAGS,
                EMPTY_METRIC_VALUES,
                !DELETED,
                tagsExistenceVerifier,
                metricsExistenceVerifier,
                validator
        );

        // then
        assertThatCode(() -> validator.validate(activity)).doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenEndTimeBeforeStartTime() {
        // given
        Instant startTime = Instant.ofEpochMilli(2000);
        Instant endTime = Instant.ofEpochMilli(1000);
        Activity activity = new Activity(
                new ActivityId(),
                CREATOR,
                ACTIVITY_TITLE,
                startTime,
                endTime,
                EMPTY_COMMENT,
                EMPTY_TAGS,
                EMPTY_METRIC_VALUES,
                !DELETED,
                tagsExistenceVerifier,
                metricsExistenceVerifier,
                validator
        );
        List<String> validationErrors = List.of(END_BEFORE_START_ERROR);

        // then
        assertThatThrownBy(() -> validator.validate(activity))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }

    @Test
    void shouldFailWhenActivityContainsNonExistingTag() {
        // given
        TagId nonExistingTag = new TagId();

        when(tagsExistenceVerifier.notExisting(any()))
                .thenReturn(singleton(nonExistingTag));

        Activity activity = new Activity(
                new ActivityId(),
                CREATOR,
                ACTIVITY_TITLE,
                EMPTY_START_TIME,
                EMPTY_END_TIME,
                EMPTY_COMMENT,
                singleton(nonExistingTag),
                EMPTY_METRIC_VALUES,
                !DELETED,
                tagsExistenceVerifier,
                metricsExistenceVerifier,
                validator
        );
        List<String> validationErrors = List.of(
                NOT_EXISTING_TAGS_ERROR.formatted(singleton(nonExistingTag))
        );

        // then
        assertThatThrownBy(() -> validator.validate(activity))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }

    @Test
    void shouldFailWhenActivityContainsValueOfNonExistingMetric() {
        // given
        TagId existingTag = new TagId();
        MetricId nonExistingMetric = new MetricId();
        MetricValue valueOfNonExistingMetric = new MetricValue(nonExistingMetric.id(), TEN);

        when(metricsExistenceVerifier.notExisting(any(), any()))
                .thenReturn(singleton(nonExistingMetric));

        Activity activity = new Activity(
                new ActivityId(),
                CREATOR,
                ACTIVITY_TITLE,
                EMPTY_START_TIME,
                EMPTY_END_TIME,
                EMPTY_COMMENT,
                singleton(existingTag),
                singleton(valueOfNonExistingMetric),
                !DELETED,
                tagsExistenceVerifier,
                metricsExistenceVerifier,
                validator
        );
        List<String> validationErrors = List.of(
                NOT_EXISTING_METRICS_ERROR.formatted(singleton(nonExistingMetric))
        );

        // then
        assertThatThrownBy(() -> validator.validate(activity))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }

    @Test
    void shouldFailWhenMultipleErrorOccurred() {
        // given
        Instant startTime = Instant.ofEpochMilli(2000);
        Instant endTime = Instant.ofEpochMilli(1000);
        TagId nonExistingTag = new TagId();
        MetricId nonExistingMetric = new MetricId();
        MetricValue valueOfNonExistingMetric = new MetricValue(nonExistingMetric.id(), TEN);

        when(tagsExistenceVerifier.notExisting(any()))
                .thenReturn(singleton(nonExistingTag));
        when(metricsExistenceVerifier.notExisting(any(), any()))
                .thenReturn(singleton(nonExistingMetric));

        Activity activity = new Activity(
                new ActivityId(),
                CREATOR,
                ACTIVITY_TITLE,
                startTime,
                endTime,
                EMPTY_COMMENT,
                singleton(nonExistingTag),
                singleton(valueOfNonExistingMetric),
                !DELETED,
                tagsExistenceVerifier,
                metricsExistenceVerifier,
                validator
        );
        List<String> validationErrors = List.of(
                END_BEFORE_START_ERROR,
                NOT_EXISTING_TAGS_ERROR.formatted(singleton(nonExistingTag)),
                NOT_EXISTING_METRICS_ERROR.formatted(singleton(nonExistingMetric))
        );

        // then
        assertThatThrownBy(() -> validator.validate(activity))
                .isInstanceOf(EntityInvalidException.class)
                .hasMessage(VALIDATION_ERROR.formatted(validationErrors));
    }
}