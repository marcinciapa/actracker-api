package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.repository.jpa.tag.TagEntity;

import java.util.*;

import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

class ChartMapper {

    List<Chart> toValueObjects(Collection<ChartEntity> entities) {
        return requireNonNullElse(entities, new ArrayList<ChartEntity>()).stream()
                .map(this::toValueObject)
                .toList();
    }

    Chart toValueObject(ChartEntity entity) {
        Set<UUID> entityTags = requireNonNullElse(entity.tags, new HashSet<TagEntity>()).stream()
                .map(tag -> tag.id)
                .map(UUID::fromString)
                .collect(toUnmodifiableSet());
        return new Chart(entity.name, Chart.GroupBy.valueOf(entity.groupBy), entityTags);
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
        entity.id = UUID.randomUUID().toString();
        entity.name = chart.name();
        entity.dashboard = dashboard;
        entity.groupBy = chart.groupBy().toString();
        entity.tags = dtoTags;
        return entity;
    }

    private TagEntity toTagEntity(String tagId) {
        TagEntity tagEntity = new TagEntity();
        tagEntity.id = tagId;
        return tagEntity;
    }
}
