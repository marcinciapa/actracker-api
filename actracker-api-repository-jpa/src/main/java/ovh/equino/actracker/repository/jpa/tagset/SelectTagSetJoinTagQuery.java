package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaPredicate;
import ovh.equino.actracker.repository.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.repository.jpa.JpaSortBuilder;
import ovh.equino.actracker.repository.jpa.MultiResultJpaQuery;

import java.util.Collection;
import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;

final class SelectTagSetJoinTagQuery extends MultiResultJpaQuery<TagSetEntity, TagSetJoinTagProjection> {

    private final Join<TagSetEntity, ?> tag;
    private final PredicateBuilder predicateBuilder;

    SelectTagSetJoinTagQuery(EntityManager entityManager) {
        super(entityManager);
        this.tag = root.join("tags", INNER);
        this.predicateBuilder = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                this.criteriaBuilder.construct(
                        TagSetJoinTagProjection.class,
                        tag.get("id"),
                        root.get("id")
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicateBuilder;
    }

    /**
     * Deprecated: Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<TagSetEntity> sort() {
        throw new RuntimeException("Sorting tag sets joint with tags not supported");
    }

    @Override
    public SelectTagSetJoinTagQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<TagSetEntity> getRootEntityType() {
        return TagSetEntity.class;
    }

    @Override
    protected Class<TagSetJoinTagProjection> getProjectionType() {
        return TagSetJoinTagProjection.class;
    }

    public class PredicateBuilder extends JpaPredicateBuilder<TagSetEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        @Override
        public JpaPredicate isNotDeleted() {
            return and(
                    super.isNotDeleted(),
                    () -> criteriaBuilder.isFalse(tag.get("deleted"))
            );
        }

        @Override
        public JpaPredicate isAccessibleFor(User user) {
            return and(
                    super.isAccessibleFor(user),
                    () -> criteriaBuilder.equal(tag.get("creatorId"), user.id().toString())
            );
        }

        public JpaPredicate hasTagSetId(UUID tagSetId) {
            return super.hasId(tagSetId);
        }

        public JpaPredicate hasTagSetIdIn(Collection<UUID> tagSetIds) {
            return super.hasIdIn(tagSetIds);
        }
    }
}
