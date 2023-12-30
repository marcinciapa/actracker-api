package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.dashboard.AnalysisMetric;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.ChartId;
import ovh.equino.actracker.domain.dashboard.GroupBy;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class ChartMapper {

    List<Chart> toDomainObjects(Collection<ChartEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<ChartEntity>()).stream()
                .map(this::toDomainObject)
                .toList();
    }

    Chart toDomainObject(ChartEntity entity) {
        if(isNull(entity)) {
            return null;
        }
        Set<UUID> entityTags = requireNonNullElse(entity.tags, new HashSet<TagEntity>()).stream()
                .map(tag -> tag.id)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());
        return new Chart(
                new ChartId(entity.id),
                entity.name,
                GroupBy.valueOf(entity.groupBy),
                AnalysisMetric.valueOf(entity.metric),
                entityTags,
                entity.deleted
        );
    }

    List<ChartEntity> toEntities(Collection<Chart> charts, DashboardEntity dashboard) {
        return requireNonNullElse(charts, new ArrayList<Chart>()).stream()
                .map(chart -> toEntity(chart, dashboard))
                .toList();
    }

    ChartEntity toEntity(Chart chart, DashboardEntity dashboard) {
        Set<TagEntity> dtoTags = requireNonNullElse(chart.includedTags(), new HashSet<UUID>()).stream()
                .map(UUID::toString)
                .map(this::toTagEntity)
                .collect(toUnmodifiableSet());

        ChartEntity entity = new ChartEntity();
        entity.id = chart.id().toString();
        entity.name = chart.name();
        entity.dashboard = dashboard;
        entity.groupBy = chart.groupBy().toString();
        entity.metric = chart.analysisMetric().toString();
        entity.tags = dtoTags;
        entity.deleted = chart.isDeleted();
        return entity;
    }

    private TagEntity toTagEntity(String tagId) {
        TagEntity tagEntity = new TagEntity();
        tagEntity.id = tagId;
        return tagEntity;
    }
}
