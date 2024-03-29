package ovh.equino.actracker.domain.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardTest {

    private static final User CREATOR = new User(randomUUID());
    private static final String DASHBOARD_NAME = "dashboard name";
    private static final List<Chart> EMPTY_CHARTS = emptyList();
    private static final List<Share> EMPTY_SHARES = emptyList();
    private static final boolean DELETED = true;

    @Mock
    private ActorExtractor actorExtractor;
    @Mock
    private DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier;
    @Mock
    private TagsAccessibilityVerifier tagsAccessibilityVerifier;
    @Mock
    private DashboardValidator validator;

    @BeforeEach
    void init() {
        when(actorExtractor.getActor()).thenReturn(CREATOR);
    }

    @Nested
    @DisplayName("rename")
    class RenameDashboardTest {

        private static final String NEW_NAME = "new dashboard name";

        @Test
        void shouldRenameDashboard() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.rename(NEW_NAME);

            // then
            assertThat(dashboard.name()).isEqualTo(NEW_NAME);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.rename(NEW_NAME))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> dashboard.rename(NEW_NAME))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> dashboard.rename(NEW_NAME))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteDashboardTest {

        private static final String CHART_NAME = "chart name";
        private static final Set<UUID> EMPTY_TAGS = emptySet();

        @Test
        void shouldDeleteDashboardAndCharts() {
            // given
            Chart existingChart = new Chart(
                    new ChartId(),
                    CHART_NAME,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS,
                    !DELETED
            );
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    singletonList(existingChart),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.delete();

            // then
            assertThat(dashboard.deleted()).isTrue();
            assertThat(dashboard.charts()).allSatisfy(
                    chart -> assertThat(chart.isDeleted()).isTrue()
            );
        }

        @Test
        void shouldLeaveDashboardUnchangedWhenAlreadyDeleted() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.delete();

            // then
            assertThat(dashboard.deleted()).isTrue();
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(dashboard::delete)
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(dashboard::delete)
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(dashboard::delete)
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("share")
    class ShareDashboardTest {

        private static final String GRANTEE_NAME = "grantee name";

        @Test
        void shouldAddNewShareWithoutId() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);

            // when
            dashboard.share(newShare);

            // then
            assertThat(dashboard.shares()).containsExactly(newShare);
        }

        @Test
        void shouldAddNewShareWithId() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);

            // when
            dashboard.share(newShare);

            // then
            assertThat(dashboard.shares()).containsExactly(newShare);
        }

        @Test
        void shouldNotAddNewShareWithIdWhenShareWithIdAlreadyExists() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);

            // when
            dashboard.share(newShare);

            // then
            assertThat(dashboard.shares()).containsExactlyInAnyOrder(existingShare);
        }

        @Test
        void shouldNotAddNewShareWithIdWhenShareWithoutIdAlreadyExists() {
            // given
            Share existingShare = new Share(GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Share newShare = new Share(new User(randomUUID()), GRANTEE_NAME);

            // when
            dashboard.share(newShare);

            // then
            assertThat(dashboard.shares()).containsExactlyInAnyOrder(existingShare);
        }

        @Test
        void shouldNotAddNewShareWithoutIdWhenShareWithIdAlreadyExists() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);

            // when
            dashboard.share(newShare);

            // then
            assertThat(dashboard.shares()).containsExactly(existingShare);
        }

        @Test
        void shouldNotAddNewShareWithoutIdWhenShareWithoutIdAlreadyExists() {
            // given
            Share existingShare = new Share(GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);

            // when
            dashboard.share(newShare);

            // then
            assertThat(dashboard.shares()).containsExactly(existingShare);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.share(newShare))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Share newShare = new Share(GRANTEE_NAME);
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> dashboard.share(newShare))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            Share newShare = new Share(GRANTEE_NAME);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> dashboard.share(newShare))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("unshare")
    class UnshareDashboardTest {

        private static final String GRANTEE_NAME = "grantee name";

        @Test
        void shouldUnshareTagWhenSharedWithId() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.unshare(existingShare.granteeName());

            // then
            assertThat(dashboard.shares()).isEmpty();
        }

        @Test
        void shouldUnshareTagWhenSharedWithoutId() {
            // given
            Share existingShare = new Share(GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.unshare(existingShare.granteeName());

            // then
            assertThat(dashboard.shares()).isEmpty();

        }

        @Test
        void shouldLeaveSharesUnchangedWhenNotShared() {
            // given
            Share existingShare1 = new Share("%s_1".formatted(GRANTEE_NAME));
            Share existingShare2 = new Share(new User(randomUUID()), "%s_2".formatted(GRANTEE_NAME));
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    List.of(existingShare1, existingShare2),
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.unshare(GRANTEE_NAME);

            // then
            assertThat(dashboard.shares()).containsExactlyInAnyOrder(existingShare1, existingShare2);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.unshare(GRANTEE_NAME))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> dashboard.unshare(existingShare.granteeName()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Share existingShare = new Share(new User(randomUUID()), GRANTEE_NAME);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    singletonList(existingShare),
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> dashboard.unshare(existingShare.granteeName()))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("addChart")
    class AddChartTest {

        private static final String CHART_NAME = "chart name";
        private static final Set<UUID> EMPTY_TAGS = emptySet();

        @Test
        void shouldAddFirstChart() {
            // given
            Chart newChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, Set.of(new TagId().id()));
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    CHART_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.nonAccessibleFor(any(), any())).thenReturn(emptySet());

            // when
            dashboard.addChart(newChart);

            // then
            assertThat(dashboard.charts()).containsExactly(newChart);
        }

        @Test
        void shouldAddAnotherChart() {
            // given
            Chart existingNonDeletedChart = new Chart(
                    CHART_NAME + 1,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );
            Chart existingDeletedChart = new Chart(
                    CHART_NAME + 2,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            ).deleted();

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    List.of(existingNonDeletedChart, existingDeletedChart),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            Chart newChart = new Chart(CHART_NAME + 3, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);

            when(tagsAccessibilityVerifier.nonAccessibleFor(any(), any())).thenReturn(emptySet());

            // when
            dashboard.addChart(newChart);

            // then
            assertThat(dashboard.charts())
                    .containsExactlyInAnyOrder(existingNonDeletedChart, existingDeletedChart, newChart);
        }

        @Test
        void shouldFailWhenAddingChartWithNonAccessibleTag() {
            // given
            TagId nonAccessibleTag = new TagId();
            Chart newChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, Set.of(nonAccessibleTag.id()));
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    CHART_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.nonAccessibleFor(any(), any())).thenReturn(Set.of(nonAccessibleTag));

            // then
            assertThatThrownBy(() -> dashboard.addChart(newChart))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Chart newChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.nonAccessibleFor(any(), any())).thenReturn(emptySet());
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.addChart(newChart))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void
        shouldFailWhenNotAccessibleToUser() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Chart newChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> dashboard.addChart(newChart))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            Chart newChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> dashboard.addChart(newChart))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("deleteChart")
    class DeleteChartTest {

        private static final String CHART_NAME = "chart name";
        private static final Set<UUID> EMPTY_TAGS = emptySet();

        @Test
        void shouldDeleteExistingChart() {
            // given
            Chart existingNonDeletedChart = new Chart(
                    CHART_NAME + 1,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );
            Chart existingDeletedChart = new Chart(
                    CHART_NAME + 2,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            ).deleted();
            Chart chartToDelete = new Chart(
                    CHART_NAME + 3,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    List.of(existingNonDeletedChart, existingDeletedChart, chartToDelete),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.deleteChart(chartToDelete.id());

            // then
            assertThat(dashboard.charts())
                    .extracting(Chart::id, Chart::isDeleted)
                    .containsExactlyInAnyOrder(
                            tuple(existingNonDeletedChart.id(), !DELETED),
                            tuple(existingDeletedChart.id(), DELETED),
                            tuple(chartToDelete.id(), DELETED)
                    );
        }

        @Test
        void shouldKeepChartsEmptyWhenRemovingFromEmptyCharts() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.deleteChart(new ChartId());

            // then
            assertThat(dashboard.charts()).isEmpty();
        }

        @Test
        void shouldKeepChartsUnchangedWhenDeletingNotExistingChart() {
            // given
            Chart existingChart = new Chart(CHART_NAME, GroupBy.SELF, AnalysisMetric.METRIC_VALUE, EMPTY_TAGS);
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    singletonList(existingChart),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.deleteChart(new ChartId());

            // then
            assertThat(dashboard.charts()).containsExactly(existingChart);
        }

        @Test
        void shouldKeepChartsUnchangedWhenDeletingAlreadyDeletedChart() {
            // given
            Chart existingDeletedChart = new Chart(
                    CHART_NAME + 1,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            ).deleted();
            Chart existingNonDeletedChart = new Chart(
                    CHART_NAME + 2,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    List.of(existingNonDeletedChart, existingDeletedChart),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            dashboard.deleteChart(existingDeletedChart.id());

            // then
            assertThat(dashboard.charts()).containsExactlyInAnyOrder(existingDeletedChart, existingNonDeletedChart);
        }

        @Test
        void shouldFailWhenDashboardInvalid() {
            // given
            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    EMPTY_CHARTS,
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> dashboard.deleteChart(new ChartId()))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            Chart existingChart = new Chart(
                    CHART_NAME,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    List.of(existingChart),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> dashboard.deleteChart(existingChart.id()))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            Chart existingChart = new Chart(
                    CHART_NAME,
                    GroupBy.SELF,
                    AnalysisMetric.METRIC_VALUE,
                    EMPTY_TAGS
            );

            Dashboard dashboard = new Dashboard(
                    new DashboardId(),
                    CREATOR,
                    DASHBOARD_NAME,
                    List.of(existingChart),
                    EMPTY_SHARES,
                    !DELETED,
                    actorExtractor,
                    dashboardsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(dashboardsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> dashboard.deleteChart(existingChart.id()))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }
}
