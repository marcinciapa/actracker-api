package ovh.equino.actracker.application.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

public class ActivityApplicationService {

    private final ActivityRepository activityRepository;
    private final ActivitySearchEngine activitySearchEngine;
    private final TagRepository tagRepository;
    private final IdentityProvider identityProvider;

    public ActivityApplicationService(ActivityRepository activityRepository,
                                      ActivitySearchEngine activitySearchEngine,
                                      TagRepository tagRepository,
                                      IdentityProvider identityProvider) {

        this.activityRepository = activityRepository;
        this.activitySearchEngine = activitySearchEngine;
        this.tagRepository = tagRepository;
        this.identityProvider = identityProvider;
    }

    public ActivityDto createActivity(ActivityDto newActivityData) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User creator = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, creator);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        Activity activity = Activity.create(newActivityData, creator, tagsExistenceVerifier, metricsExistenceVerifier);
        activityRepository.add(activity.forStorage());
        return activity.forClient(creator);
    }

    public EntitySearchResult<ActivityDto> searchActivities(EntitySearchCriteria searchCriteria) {
        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, searchCriteria.searcher());
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        EntitySearchResult<ActivityDto> searchResult = activitySearchEngine.findActivities(searchCriteria);
        List<ActivityDto> resultForClient = searchResult.results().stream()
                .map(activity -> Activity.fromStorage(activity, tagsExistenceVerifier, metricsExistenceVerifier))
                .map(activity -> activity.forClient(searchCriteria.searcher()))
                .toList();

        return new EntitySearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public ActivityDto switchToNewActivity(ActivityDto activityToSwitch) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User switcher = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, switcher);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        Activity newActivity = Activity.create(activityToSwitch, switcher, tagsExistenceVerifier, metricsExistenceVerifier);
        Instant switchTime = newActivity.isStarted() ? newActivity.startTime() : now();
        newActivity.start(switchTime, switcher);

        List<Activity> activitiesToFinish = activityRepository.findUnfinishedStartedBefore(switchTime, switcher).stream()
                .map(activity -> Activity.fromStorage(activity, tagsExistenceVerifier, metricsExistenceVerifier))
                .toList();

        activitiesToFinish.forEach(activity -> activity.finish(switchTime, switcher));
        activitiesToFinish.stream()
                .map(Activity::forStorage)
                .forEach(activity -> activityRepository.update(activity.id(), activity));

        activityRepository.add(newActivity.forStorage());

        return newActivity.forClient(switcher);
    }

    public ActivityDto renameActivity(String newTitle, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.rename(newTitle, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto startActivity(Instant startTime, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.start(startTime, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto finishActivity(Instant endTime, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.finish(endTime, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto updateActivityComment(String newComment, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.updateComment(newComment, updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto addTagToActivity(UUID tagId, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.assignTag(new TagId(tagId), updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto removeTagFromActivity(UUID tagId, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.removeTag(new TagId(tagId), updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto setMetricValue(UUID metricId, BigDecimal value, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.setMetricValue(new MetricValue(metricId, value), updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public ActivityDto unsetMetricValue(UUID metricId, UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, updater);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.unsetMetricValue(new MetricId(metricId), updater);

        activityRepository.update(activityId, activity.forStorage());
        return activity.forClient(updater);
    }

    public void deleteActivity(UUID activityId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User remover = new User(requesterIdentity.getId());

        TagsExistenceVerifier tagsExistenceVerifier = new TagsExistenceVerifier(tagRepository, remover);
        MetricsExistenceVerifier metricsExistenceVerifier = new MetricsExistenceVerifier(tagsExistenceVerifier);

        ActivityDto activityDto = activityRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException(Activity.class, activityId));

        Activity activity = Activity.fromStorage(activityDto, tagsExistenceVerifier, metricsExistenceVerifier);
        activity.delete(remover);

        activityRepository.update(activityId, activity.forStorage());
    }
}
