package ovh.equino.actracker.domain.activity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository {

    void add(ActivityDto activity);

    void udpate(UUID activityId, ActivityDto activity);

    Optional<ActivityDto> findById(UUID activityId);

    List<ActivityDto> findAll();
}
