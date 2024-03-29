package ovh.equino.actracker.application.tagset;

import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tagset.*;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.*;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagSetApplicationService {

    private final TagSetFactory tagSetFactory;
    private final TagSetRepository tagSetRepository;
    private final TagSetDataSource tagSetDataSource;
    private final TagSetSearchEngine tagSetSearchEngine;
    private final TagSetNotifier tagSetNotifier;
    private final ActorExtractor actorExtractor;

    public TagSetApplicationService(TagSetFactory tagSetFactory,
                                    TagSetRepository tagSetRepository,
                                    TagSetDataSource tagSetDataSource,
                                    TagSetSearchEngine tagSetSearchEngine,
                                    TagSetNotifier tagSetNotifier,
                                    ActorExtractor actorExtractor) {

        this.tagSetFactory = tagSetFactory;
        this.tagSetRepository = tagSetRepository;
        this.tagSetDataSource = tagSetDataSource;
        this.tagSetSearchEngine = tagSetSearchEngine;
        this.tagSetNotifier = tagSetNotifier;
        this.actorExtractor = actorExtractor;
    }

    public TagSetResult getTagSet(UUID tagSetId) {
        return findTagSetResult(new TagSetId(tagSetId))
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));
    }

    public Optional<TagSetResult> findTagSetResult(TagSetId tagSetId) {
        User actor = actorExtractor.getActor();
        return tagSetDataSource.find(tagSetId, actor).map(this::toTagSetResult);
    }

    private TagSetResult toTagSetResult(TagSetDto tagSetResult) {
        return new TagSetResult(tagSetResult.id(), tagSetResult.name(), tagSetResult.tags());
    }

    public TagSetResult createTagSet(CreateTagSetCommand createTagSetCommand) {
        TagSet newTagSet = tagSetFactory.create(
                createTagSetCommand.name(),
                toTagIds(createTagSetCommand.tags())
        );
        tagSetRepository.add(newTagSet);
        tagSetNotifier.notifyChanged(newTagSet.forChangeNotification());

        return findTagSetResult(newTagSet.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find created tag set with ID=%s".formatted(newTagSet.id())
                ));
    }

    private Set<TagId> toTagIds(Collection<UUID> uuids) {
        return uuids.stream()
                .map(TagId::new)
                .collect(toUnmodifiableSet());
    }

    public SearchResult<TagSetResult> searchTagSets(SearchTagSetsQuery searchTagSetsQuery) {

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                actorExtractor.getActor(),
                searchTagSetsQuery.pageSize(),
                searchTagSetsQuery.pageId(),
                searchTagSetsQuery.term(),
                null,
                null,
                searchTagSetsQuery.excludeFilter(),
                null
        );
        EntitySearchResult<TagSetDto> searchResult = tagSetSearchEngine.findTagSets(searchCriteria);
        List<TagSetResult> resultForClient = searchResult.results()
                .stream()
                .map(this::toTagSetResult)
                .toList();

        return new SearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public TagSetResult renameTagSet(String newName, UUID tagSetId) {
        TagSet tagSet = tagSetRepository.get(new TagSetId(tagSetId))
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));

        tagSet.rename(newName);
        tagSetRepository.save(tagSet);
        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

        return findTagSetResult(tagSet.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated tag set with ID=%s".formatted(tagSet.id())
                ));
    }

    public TagSetResult addTagToSet(UUID tagId, UUID tagSetId) {
        TagSet tagSet = tagSetRepository.get(new TagSetId(tagSetId))
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));

        tagSet.assignTag(new TagId(tagId));
        tagSetRepository.save(tagSet);
        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

        return findTagSetResult(tagSet.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated tag set with ID=%s".formatted(tagSet.id())
                ));
    }

    public TagSetResult removeTagFromSet(UUID tagId, UUID tagSetId) {
        TagSet tagSet = tagSetRepository.get(new TagSetId(tagSetId))
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));

        tagSet.removeTag(new TagId(tagId));
        tagSetRepository.save(tagSet);
        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());

        return findTagSetResult(tagSet.id())
                .orElseThrow(() -> new RuntimeException(
                        "Could not find updated tag set with ID=%s".formatted(tagSet.id())
                ));
    }

    public void deleteTagSet(UUID tagSetId) {
        TagSet tagSet = tagSetRepository.get(new TagSetId(tagSetId))
                .orElseThrow(() -> new EntityNotFoundException(TagSet.class, tagSetId));

        tagSet.delete();
        tagSetRepository.save(tagSet);
        tagSetNotifier.notifyChanged(tagSet.forChangeNotification());
    }
}
