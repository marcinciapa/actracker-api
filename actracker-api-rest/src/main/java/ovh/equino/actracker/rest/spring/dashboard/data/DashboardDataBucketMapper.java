package ovh.equino.actracker.rest.spring.dashboard.data;

import ovh.equino.actracker.domain.dashboard.ChartBucketData;
import ovh.equino.actracker.rest.spring.PayloadMapper;

import java.util.Collection;
import java.util.LinkedList;

import static java.util.Objects.requireNonNullElse;

class DashboardDataBucketMapper extends PayloadMapper {

    DashboardDataBucket toResponse(ChartBucketData bucket) {
        return new DashboardDataBucket(
                bucket.name(),
                bucket.value(),
                bucket.percentage()
        );
    }

    Collection<DashboardDataBucket> toResponse(Collection<ChartBucketData> buckets) {
        return requireNonNullElse(buckets, new LinkedList<ChartBucketData>()).stream()
                .map(this::toResponse)
                .toList();
    }
}