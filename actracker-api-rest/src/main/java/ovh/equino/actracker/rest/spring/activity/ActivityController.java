package ovh.equino.actracker.rest.spring.activity;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivitySortField;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.rest.spring.EntitySearchCriteriaBuilder;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.actracker.rest.spring.tag.Tag;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/activity")
class ActivityController {

    private final ActivityApplicationService activityApplicationService;
    private final IdentityProvider identityProvider;
    private final ActivityMapper mapper = new ActivityMapper();

    ActivityController(ActivityApplicationService activityApplicationService, IdentityProvider identityProvider) {
        this.activityApplicationService = activityApplicationService;
        this.identityProvider = identityProvider;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Activity createActivity(@RequestBody Activity activity) {
        ActivityDto activityDto = mapper.fromRequest(activity);
        ActivityDto createdActivity = activityApplicationService.createActivity(activityDto);
        return mapper.toResponse(createdActivity);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<Activity> searchActivities(
            @RequestParam(name = "pageId", required = false) String pageId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "requiredTags", required = false) String requiredTags,
            @RequestParam(name = "excludedActivities", required = false) String excludedActivities,
            @RequestParam(name = "rangeStartMillis", required = false) Long rangeStartMillis,
            @RequestParam(name = "rangeEndMillis", required = false) Long rangeEndMillis,
            @RequestParam(name = "orderBy", required = false) String orderBy) {

        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        EntitySearchCriteria searchCriteria = new EntitySearchCriteriaBuilder()
                .withSearcher(requester)
                .withPageId(pageId)
                .withPageSize(pageSize)
                .withTerm(term)
                .withTimeRangeStart(rangeStartMillis)
                .withTimeRangeEnd(rangeEndMillis)
                .withExcludedIdsJointWithComma(excludedActivities)
                .withTagsJointWithComma(requiredTags)
                .withPossibleSortFields(ActivitySortField.START_TIME)
//                .withSortLevelsJointWithComma(orderBy)
                .build();

        EntitySearchResult<ActivityDto> searchResult = activityApplicationService.searchActivities(searchCriteria);
        return mapper.toResponse(searchResult);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    @ResponseStatus(OK)
    void deleteActivity(@PathVariable("id") String id) {
        activityApplicationService.deleteActivity(UUID.fromString(id));
    }

    @RequestMapping(method = POST, path = "/switched")
    @ResponseStatus(OK)
    Activity switchToActivity(@RequestBody Activity activity) {
        ActivityDto activityDto = mapper.fromRequest(activity);
        ActivityDto switchedActivity = activityApplicationService.switchToNewActivity(activityDto);
        return mapper.toResponse(switchedActivity);
    }

    @RequestMapping(method = PUT, path = "/{id}/title")
    @ResponseStatus(OK)
    Activity renameActivity(@PathVariable("id") String id, @RequestBody String newTitle) {
        ActivityDto activity = activityApplicationService.renameActivity(newTitle, UUID.fromString(id));
        return mapper.toResponse(activity);
    }

    @RequestMapping(method = PUT, path = "/{id}/startTime")
    @ResponseStatus(OK)
    Activity startActivity(@PathVariable("id") String id, @RequestBody Long newStartTimestamp) {
        Instant newStartTime = Instant.ofEpochMilli(newStartTimestamp);
        ActivityDto activity = activityApplicationService.startActivity(newStartTime, UUID.fromString(id));
        return mapper.toResponse(activity);
    }

    @RequestMapping(method = PUT, path = "/{id}/endTime")
    @ResponseStatus(OK)
    Activity finishActivity(@PathVariable("id") String id, @RequestBody Long newEndTimestamp) {
        Instant newEndTime = Instant.ofEpochMilli(newEndTimestamp);
        ActivityDto activity = activityApplicationService.finishActivity(newEndTime, UUID.fromString(id));
        return mapper.toResponse(activity);
    }

    @RequestMapping(method = PUT, path = "/{id}/comment")
    @ResponseStatus(OK)
    Activity updateActivityComment(@PathVariable("id") String id, @RequestBody String newComment) {
        ActivityDto activity = activityApplicationService.updateActivityComment(newComment, UUID.fromString(id));
        return mapper.toResponse(activity);
    }

    @RequestMapping(method = POST, path = "/{id}/tag")
    @ResponseStatus(OK)
    Activity addTagToActivity(@PathVariable("id") String id, @RequestBody Tag newTag) {
        UUID tagId = UUID.fromString(newTag.id());
        ActivityDto activity = activityApplicationService.addTagToActivity(tagId, UUID.fromString(id));
        return mapper.toResponse(activity);
    }

    @RequestMapping(method = DELETE, path = "/{activityId}/tag/{tagId}")
    @ResponseStatus(OK)
    Activity removeTagFromActivity(@PathVariable("activityId") String activityId, @PathVariable("tagId") String tagId) {
        ActivityDto activity = activityApplicationService.removeTagFromActivity(
                UUID.fromString(tagId),
                UUID.fromString(activityId)
        );
        return mapper.toResponse(activity);
    }

    @RequestMapping(method = PUT, path = "/{activityId}/metric/{metricId}/value")
    @ResponseStatus(OK)
    Activity setActivityMetricValue(@PathVariable("activityId") String activityId,
                                    @PathVariable("metricId") String metricId,
                                    @RequestBody BigDecimal value) {

        ActivityDto updatedActivity = activityApplicationService.setMetricValue(
                UUID.fromString(metricId),
                value,
                UUID.fromString(activityId)
        );
        return mapper.toResponse(updatedActivity);
    }

    @RequestMapping(method = DELETE, path = "/{activityId}/metric/{metricId}/value")
    @ResponseStatus(OK)
    Activity unsetActivityMetricValue(@PathVariable("activityId") String activityId,
                                      @PathVariable("metricId") String metricId) {

        ActivityDto updatedActivity = activityApplicationService.unsetMetricValue(
                UUID.fromString(metricId),
                UUID.fromString(activityId)
        );
        return mapper.toResponse(updatedActivity);
    }
}
