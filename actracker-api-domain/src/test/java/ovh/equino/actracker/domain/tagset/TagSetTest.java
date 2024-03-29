package ovh.equino.actracker.domain.tagset;

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
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagSetTest {

    private static final User CREATOR = new User(randomUUID());
    private static final String TAG_SET_NAME = "tag set name";
    private static final List<TagId> EMPTY_TAGS = emptyList();
    private static final boolean DELETED = true;

    @Mock
    private ActorExtractor actorExtractor;
    @Mock
    private TagSetsAccessibilityVerifier tagSetsAccessibilityVerifier;
    @Mock
    private TagsAccessibilityVerifier tagsAccessibilityVerifier;
    @Mock
    private TagSetValidator validator;

    @BeforeEach
    void init() {
        when(actorExtractor.getActor()).thenReturn(CREATOR);
    }

    @Nested
    @DisplayName("rename")
    class RenameTest {

        private static final String NEW_NAME = "tag set new name";

        @Test
        void shouldChangeName() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tagSet.rename(NEW_NAME);

            // then
            assertThat(tagSet.name()).isEqualTo(NEW_NAME);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tagSet.rename(NEW_NAME))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagSetsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> tagSet.rename(NEW_NAME))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagSetsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> tagSet.rename(NEW_NAME))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("assignTag")
    class AssignTagTest {

        @Test
        void shouldAssignFirstTag() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            TagId newTag = new TagId(randomUUID());
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            tagSet.assignTag(newTag);

            // then
            assertThat(tagSet.tags()).containsExactly(newTag);
        }

        @Test
        void shouldAssignAnotherTag() {
            // given
            TagId existingTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    singleton(existingTag),
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);
            TagId newTag = new TagId();

            // when
            tagSet.assignTag(newTag);

            // then
            assertThat(tagSet.tags()).containsExactlyInAnyOrder(existingTag, newTag);
        }

        @Test
        void shouldNotDuplicateAssignedTag() {
            // given
            TagId existingTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    singleton(existingTag),
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            tagSet.assignTag(existingTag);

            // then
            assertThat(tagSet.tags()).containsExactly(existingTag);
        }

        @Test
        void shouldFailWhenAssigningNonAccessibleTag() {
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            TagId newTag = new TagId(randomUUID());
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> tagSet.assignTag(newTag))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenEntityInvalid() {
            // given
            TagId newTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tagSet.assignTag(newTag))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagSetsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);
            TagId newTag = new TagId();

            // then
            assertThatThrownBy(() -> tagSet.assignTag(newTag))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagSetsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);
            TagId newTag = new TagId();

            // then
            assertThatThrownBy(() -> tagSet.assignTag(newTag))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("removeTag")
    class RemoveTagTest {

        @Test
        void shouldRemoveAssignedTag() {
            // given
            TagId tagToPreserve = new TagId();
            TagId tagToRemove = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    List.of(tagToPreserve, tagToRemove),
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            tagSet.removeTag(tagToRemove);

            // then
            assertThat(tagSet.tags()).containsExactly(tagToPreserve);
        }

        @Test
        void shouldKeepTagsEmptyWhenRemovingFromEmptyTags() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            tagSet.removeTag(new TagId());

            // then
            assertThat(tagSet.tags()).isEmpty();
        }

        @Test
        void shouldKeepTagsUnchangedWhenRemovingUnassignedTag() {
            // given
            TagId existingTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    singleton(existingTag),
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // when
            tagSet.removeTag(new TagId());

            // then
            assertThat(tagSet.tags()).containsExactly(existingTag);
        }

        @Test
        void shouldKeepTagsUnchangedWhenRemovingNonAccessibleTag() {
            // given
            TagId tagToRemove = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    List.of(tagToRemove),
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // when
            tagSet.removeTag(tagToRemove);

            // then
            assertThat(tagSet.tags()).containsExactly(tagToRemove);
        }

        @Test
        void shouldFailWhenTagSetInvalid() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            when(tagsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(() -> tagSet.removeTag(new TagId()))
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            TagId existingTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    singleton(existingTag),
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagSetsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(() -> tagSet.removeTag(existingTag))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            TagId existingTag = new TagId();
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    singleton(existingTag),
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagSetsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(() -> tagSet.removeTag(existingTag))
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        void shouldDeleteTagSet() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );

            // when
            tagSet.delete();

            // then
            assertThat(tagSet.deleted()).isTrue();
        }

        @Test
        void shouldFailWhenTagSetInvalid() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            doThrow(EntityInvalidException.class).when(validator).validate(any());

            // then
            assertThatThrownBy(tagSet::delete)
                    .isInstanceOf(EntityInvalidException.class);
        }

        @Test
        void shouldFailWhenNotAccessibleToUser() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagSetsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(false);

            // then
            assertThatThrownBy(tagSet::delete)
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void shouldFailWhenUserNotAllowed() {
            // given
            TagSet tagSet = new TagSet(
                    new TagSetId(),
                    CREATOR,
                    TAG_SET_NAME,
                    EMPTY_TAGS,
                    !DELETED,
                    actorExtractor,
                    tagSetsAccessibilityVerifier,
                    tagsAccessibilityVerifier,
                    validator
            );
            User unauthorizedUser = new User(randomUUID());
            when(actorExtractor.getActor()).thenReturn(unauthorizedUser);
            when(tagSetsAccessibilityVerifier.isAccessibleFor(any(), any())).thenReturn(true);

            // then
            assertThatThrownBy(tagSet::delete)
                    .isInstanceOf(EntityEditForbidden.class);
        }
    }
}
