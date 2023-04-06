package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.UUID;

public interface TagService {

    TagDto createTag(TagDto newTagData, User creator);

    TagDto updateTag(UUID tagId, TagDto updatedTagData, User updater);

    List<TagDto> getTags(User searcher);

    void deleteTag(UUID tagId, User remover);
}