package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.MetricDto;
import ovh.equino.actracker.domain.tag.MetricType;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static ovh.equino.actracker.repository.jpa.TestUtil.randomString;

public final class TagBuilder {

    private TagDto newTag;

    TagBuilder(TenantDto creator) {
        this.newTag = new TagDto(
                randomUUID(),
                creator.id(),
                randomString(),
                List.of(
                        new MetricDto(randomUUID(), creator.id(), randomString(), MetricType.NUMERIC, false),
                        new MetricDto(randomUUID(), creator.id(), randomString(), MetricType.NUMERIC, false),
                        new MetricDto(randomUUID(), creator.id(), randomString(), MetricType.NUMERIC, false)
                ),
                List.of(
                        new Share(new User(randomUUID()), randomString()),
                        new Share(new User(randomUUID()), randomString())
                ),
                false
        );
    }

    public TagBuilder sharedWith(TenantDto... grantees) {
        this.newTag = new TagDto(
                newTag.id(),
                newTag.creatorId(),
                newTag.name(),
                newTag.metrics(),
                stream(grantees)
                        .map(grantee -> new Share(
                                        new User(grantee.id()),
                                        grantee.username()
                                )
                        )
                        .toList(),
                newTag.deleted()
        );

        return this;
    }

    public TagBuilder deleted() {
        this.newTag = new TagDto(
                newTag.id(),
                newTag.creatorId(),
                newTag.name(),
                newTag.metrics(),
                newTag.shares(),
                true
        );

        return this;
    }

    public TagDto build() {
        return newTag;
    }
}
