package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.application.tag.TagApplicationService;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.application",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ActivityApplicationService.class,
                        DashboardApplicationService.class,
                        TagApplicationService.class,
                        TagSetApplicationService.class
                }
        )
)
class ApplicationServiceConfiguration {

    @Bean
    ApplicationServiceMetricAspect metricAspect() {
        return new ApplicationServiceMetricAspect();
    }

    @Bean
    ApplicationServiceTransactionAspect transactionAspect() {
        return new ApplicationServiceTransactionAspect();
    }
}
