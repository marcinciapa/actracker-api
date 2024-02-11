package ovh.equino.actracker.main.springboot.configuration.repository;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.activity.ActivityRepository;
import ovh.equino.actracker.domain.dashboard.DashboardRepository;
import ovh.equino.actracker.domain.tag.TagRepository;
import ovh.equino.actracker.domain.tagset.TagSetRepository;
import ovh.equino.actracker.notification.outbox.NotificationRepository;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.repository.jpa",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ActivityRepository.class,
                        TagRepository.class,
                        NotificationRepository.class,
                        TagSetRepository.class,
                        DashboardRepository.class,
                }
        )
)
class JpaRepositoryConfiguration {
}
