package ovh.equino.actracker.application.dashboard;

import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.dashboard.*;
import ovh.equino.actracker.domain.dashboard.generation.*;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.*;

import static java.util.Objects.requireNonNullElse;

public class DashboardApplicationService {

    private final DashboardFactory dashboardFactory;
    private final DashboardRepository dashboardRepository;
    private final DashboardDataSource dashboardDataSource;
    private final DashboardSearchEngine dashboardSearchEngine;
    private final DashboardGenerationEngine dashboardGenerationEngine;
    private final DashboardNotifier dashboardNotifier;
    private final TenantDataSource tenantDataSource;
    private final ActorExtractor actorExtractor;

    public DashboardApplicationService(DashboardFactory dashboardFactory,
                                       DashboardRepository dashboardRepository,
                                       DashboardDataSource dashboardDataSource,
                                       DashboardSearchEngine dashboardSearchEngine,
                                       DashboardGenerationEngine dashboardGenerationEngine,
                                       DashboardNotifier dashboardNotifier,
                                       TenantDataSource tenantDataSource,
                                       ActorExtractor actorExtractor) {

        this.dashboardFactory = dashboardFactory;
        this.dashboardRepository = dashboardRepository;
        this.dashboardDataSource = dashboardDataSource;
        this.dashboardSearchEngine = dashboardSearchEngine;
        this.dashboardGenerationEngine = dashboardGenerationEngine;
        this.dashboardNotifier = dashboardNotifier;
        this.tenantDataSource = tenantDataSource;
        this.actorExtractor = actorExtractor;
    }

    public DashboardResult getDashboard(UUID dashboardId) {
        return findDashboardResult(new DashboardId(dashboardId))
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));
    }

    public Optional<DashboardResult> findDashboardResult(DashboardId dashboardId) {
        User actor = actorExtractor.getActor();
        return dashboardDataSource.find(dashboardId, actor).map(this::toDashboardResult);
    }

    private DashboardResult toDashboardResult(DashboardDto dashboardDto) {
        List<ChartResult> chartResults = dashboardDto.charts().stream()
                .map(this::toChartResult)
                .toList();
        List<String> shares = dashboardDto.shares().stream()
                .map(Share::granteeName)
                .toList();
        return new DashboardResult(
                dashboardDto.id(),
                dashboardDto.name(),
                chartResults,
                shares
        );
    }

    private ChartResult toChartResult(Chart chart) {
        return new ChartResult(
                chart.id().id(),
                chart.name(),
                chart.groupBy().toString(),
                chart.analysisMetric().toString(),
                chart.includedTags()
        );
    }

    public DashboardResult createDashboard(CreateDashboardCommand createDashboardCommand) {
        List<Chart> charts = createDashboardCommand.chartAssignments().stream()
                .map(chartAssignment -> new Chart(
                        chartAssignment.name(),
                        GroupBy.valueOf(chartAssignment.groupBy()),
                        AnalysisMetric.valueOf(chartAssignment.analysisMetric()),
                        chartAssignment.includedTags()))
                .toList();
        List<Share> shares = createDashboardCommand.shares()
                .stream()
                .map(Share::new)
                .toList();

        Dashboard dashboard = dashboardFactory.create(createDashboardCommand.name(), charts, shares);
        dashboardRepository.add(dashboard);
        dashboardNotifier.notifyChanged(dashboard.forChangeNotification());

        return findDashboardResult(dashboard.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find created dashboard with ID=%s".formatted(dashboard.id())
                ));
    }

    public SearchResult<DashboardResult> searchDashboards(SearchDashboardsQuery searchDashboardsQuery) {

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                actorExtractor.getActor(),
                searchDashboardsQuery.pageSize(),
                searchDashboardsQuery.pageId(),
                searchDashboardsQuery.term(),
                null,
                null,
                searchDashboardsQuery.excludeFilter(),
                null
        );

        EntitySearchResult<DashboardDto> searchResult = dashboardSearchEngine.findDashboards(searchCriteria);
        List<DashboardResult> resultForClient = searchResult.results()
                .stream()
                .map(this::toDashboardResult)
                .toList();

        return new SearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public DashboardResult renameDashboard(String newName, UUID dashboardId) {
        Dashboard dashboard = dashboardRepository.get(new DashboardId(dashboardId))
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        dashboard.rename(newName);
        dashboardRepository.save(dashboard);
        dashboardNotifier.notifyChanged(dashboard.forChangeNotification());

        return findDashboardResult(dashboard.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated dashboard with ID=%s".formatted(dashboard.id())
                ));
    }

    public void deleteDashboard(UUID dashboardId) {
        Dashboard dashboard = dashboardRepository.get(new DashboardId(dashboardId))
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        dashboard.delete();
        dashboardRepository.save(dashboard);
        dashboardNotifier.notifyChanged(dashboard.forChangeNotification());
    }

    public DashboardResult addChart(ChartAssignment newChartAssignment, UUID dashboardId) {
        Dashboard dashboard = dashboardRepository.get(new DashboardId(dashboardId))
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        Chart newChart = new Chart(
                newChartAssignment.name(),
                GroupBy.valueOf(newChartAssignment.groupBy()),
                AnalysisMetric.valueOf(newChartAssignment.analysisMetric()),
                newChartAssignment.includedTags()
        );

        dashboard.addChart(newChart);
        dashboardRepository.save(dashboard);
        dashboardNotifier.notifyChanged(dashboard.forChangeNotification());

        return findDashboardResult(dashboard.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated dashboard with ID=%s".formatted(dashboard.id())
                ));
    }

    public DashboardResult deleteChart(UUID chartId, UUID dashboardId) {
        Dashboard dashboard = dashboardRepository.get(new DashboardId(dashboardId))
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        dashboard.deleteChart(new ChartId(chartId));
        dashboardRepository.save(dashboard);
        dashboardNotifier.notifyChanged(dashboard.forChangeNotification());

        return findDashboardResult(dashboard.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated dashboard with ID=%s".formatted(dashboard.id())
                ));
    }

    public DashboardResult shareDashboard(String newGrantee, UUID dashboardId) {
        Dashboard dashboard = dashboardRepository.get(new DashboardId(dashboardId))
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        Share share = resolveShare(newGrantee);
        dashboard.share(share);
        dashboardRepository.save(dashboard);
        dashboardNotifier.notifyChanged(dashboard.forChangeNotification());

        return findDashboardResult(dashboard.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated dashboard with ID=%s".formatted(dashboard.id())
                ));
    }

    public DashboardResult unshareDashboard(String granteeName, UUID dashboardId) {
        Dashboard dashboard = dashboardRepository.get(new DashboardId(dashboardId))
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        dashboard.unshare(granteeName);
        dashboardRepository.save(dashboard);
        dashboardNotifier.notifyChanged(dashboard.forChangeNotification());

        return findDashboardResult(dashboard.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated dashboard with ID=%s".formatted(dashboard.id())
                ));
    }

    public DashboardGenerationResult generateDashboard(GenerateDashboardQuery generateDashboardQuery) {
        DashboardGenerationCriteria generationCriteria = new DashboardGenerationCriteria(
                generateDashboardQuery.dashboardId(),
                actorExtractor.getActor(),
                generateDashboardQuery.timeRangeStart(),
                generateDashboardQuery.timeRangeEnd(),
                generateDashboardQuery.tags()
        );

        UUID dashboardId = generationCriteria.dashboardId();

        Dashboard dashboard = dashboardRepository.get(new DashboardId(dashboardId))
                .orElseThrow(() -> new EntityNotFoundException(Dashboard.class, dashboardId));

        DashboardData dashboardData = dashboardGenerationEngine.generateDashboard(dashboard.forStorage(), generationCriteria);
        return toGenerationResult(dashboardData);
    }

    // TODO extract to share resolver service
    private Share resolveShare(String grantee) {
        return tenantDataSource.findByUsername(grantee)
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(grantee));
    }

    private DashboardGenerationResult toGenerationResult(DashboardData dashboardData) {
        List<GeneratedChart> generatedCharts = dashboardData.charts().stream()
                .map(this::toGeneratedChart)
                .toList();
        return new DashboardGenerationResult(dashboardData.name(), generatedCharts);
    }

    private GeneratedChart toGeneratedChart(DashboardChartData chartData) {
        return new GeneratedChart(
                chartData.name(),
                toGeneratedBuckets(chartData.buckets())
        );
    }

    private List<GeneratedBucket> toGeneratedBuckets(Collection<ChartBucketData> bucketsData) {
        return requireNonNullElse(bucketsData, new ArrayList<ChartBucketData>())
                .stream()
                .map(this::toGeneratedBucket)
                .toList();
    }

    private GeneratedBucket toGeneratedBucket(ChartBucketData bucketData) {
        return new GeneratedBucket(
                bucketData.id(),
                bucketData.rangeStart(),
                bucketData.rangeEnd(),
                bucketData.bucketType().toString(),
                bucketData.value(),
                bucketData.percentage(),
                toGeneratedBuckets(bucketData.buckets())
        );
    }
}
