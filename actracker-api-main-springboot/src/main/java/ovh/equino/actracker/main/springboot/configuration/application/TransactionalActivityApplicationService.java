package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.domain.user.ActorExtractor;

//@Transactional
@Service
class TransactionalActivityApplicationService extends ActivityApplicationService {

    TransactionalActivityApplicationService(ActivityFactory activityFactory,
                                            ActivityRepository activityRepository,
                                            ActivityDataSource activityDataSource,
                                            ActivitySearchEngine activitySearchEngine,
                                            ActivityNotifier activityNotifier,
                                            ActorExtractor actorExtractor) {

        super(
                activityFactory,
                activityRepository,
                activityDataSource,
                activitySearchEngine,
                activityNotifier,
                actorExtractor
        );
    }
}
