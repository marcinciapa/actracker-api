package ovh.equino.actracker.dashboard.generation.repository;

import ovh.equino.actracker.domain.dashboard.*;

import java.util.List;

class RepositoryDashboardGenerationEngine implements DashboardGenerationEngine {

    private final DashboardRepository dashboardRepository;

    RepositoryDashboardGenerationEngine(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardData generateDashboard(DashboardDto dashboard, DashboardGenerationParameters generationParameters) {
        List<DashboardChartData> chartsData = dashboard.charts().stream()
                .map(chart -> dashboardRepository.generateChart(chart.name(), generationParameters))
                .toList();

        return new DashboardData(dashboard.name(), chartsData);
    }
}
