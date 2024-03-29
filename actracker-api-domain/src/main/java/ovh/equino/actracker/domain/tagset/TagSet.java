package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityEditForbidden;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class TagSet implements Entity {

    private final TagSetId id;
    private final User creator;
    private String name;
    private final Set<TagId> tags;
    private boolean deleted;

    private final ActorExtractor actorExtractor;
    private final TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final TagSetValidator validator;

    TagSet(TagSetId id,
           User creator,
           String name,
           Collection<TagId> tags,
           boolean deleted,
           ActorExtractor actorExtractor,
           TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier,
           TagsAccessibilityVerifier tagsAccessibilityVerifier,
           TagSetValidator validator) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.tags = new HashSet<>(tags);
        this.deleted = deleted;

        this.actorExtractor = actorExtractor;
        this.tagSetsAccessibilityVerifier = tagSetsAccessibilityVerifier;
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.validator = validator;
    }

    public void rename(String newName) {
        User actor = actorExtractor.getActor();
        if (!creator.equals(actor) && !tagSetsAccessibilityVerifier.isAccessibleFor(actor, this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        if (!this.isEditableFor(actor)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        name = newName;
        this.validate();
    }

    public void assignTag(TagId newTag) {
        User actor = actorExtractor.getActor();
        if (!creator.equals(actor) && !tagSetsAccessibilityVerifier.isAccessibleFor(actor, this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        if (!this.isEditableFor(actor)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        if (!tagsAccessibilityVerifier.isAccessibleFor(actor, newTag)) {
            String errorMessage = "Tag with ID %s does not exist".formatted(newTag.id());
            throw new EntityInvalidException(TagSet.class, errorMessage);
        }
        tags.add(newTag);
        this.validate();
    }

    public void removeTag(TagId tag) {
        User actor = actorExtractor.getActor();
        if (!creator.equals(actor) && !tagSetsAccessibilityVerifier.isAccessibleFor(actor, this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        if (!this.isEditableFor(actor)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        if (!tagsAccessibilityVerifier.isAccessibleFor(actor, tag)) {
            return;
        }
        tags.remove(tag);
        this.validate();
    }

    public void delete() {
        User actor = actorExtractor.getActor();
        if (!creator.equals(actor) && !tagSetsAccessibilityVerifier.isAccessibleFor(actor, this.id)) {
            throw new EntityNotFoundException(TagSet.class, id.id());
        }
        if (!this.isEditableFor(actor)) {
            throw new EntityEditForbidden(TagSet.class);
        }
        this.deleted = true;
        this.validate();
    }

    // TODO remove
    public TagSetDto forStorage() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        return new TagSetDto(id.id(), creator.id(), name, tagIds, deleted);
    }

    // TODO change
    public TagSetChangedNotification forChangeNotification() {
        Set<UUID> tagIds = tags.stream()
                .map(TagId::id)
                .collect(toUnmodifiableSet());
        TagSetDto dto = new TagSetDto(id.id(), creator.id(), name, tagIds, deleted);
        return new TagSetChangedNotification(dto);
    }

    @Override
    public void validate() {
        validator.validate(this);
    }

    String name() {
        return this.name;
    }

    Set<TagId> tags() {
        return unmodifiableSet(tags);
    }

    boolean deleted() {
        return this.deleted;
    }

    @Override
    public User creator() {
        return creator;
    }

    // TODO think about extracting it to superclass
    public TagSetId id() {
        return this.id;
    }
}
