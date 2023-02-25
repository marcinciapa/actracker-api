package ovh.equino.actracker.main.springboot.configuration.messaging;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

@Configuration
@Profile("actracker-api-messagebroker-memory")
@ComponentScan(
        basePackages = "ovh.equino.actracker.messagebroker.memory",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        NotificationPublisher.class
                }
        )
)
class MemoryBrokerConfiguration {
}
