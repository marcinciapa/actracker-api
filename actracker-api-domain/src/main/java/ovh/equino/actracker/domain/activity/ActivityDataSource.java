package ovh.equino.actracker.domain.activity;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.user.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ActivityDataSource {

    Optional<ActivityDto> find(ActivityId activityId, User searcher);

    List<ActivityDto> find(EntitySearchCriteria searchCriteria);

    List<ActivityId> findOwnUnfinishedStartedBefore(Instant startTime, User user);
}
