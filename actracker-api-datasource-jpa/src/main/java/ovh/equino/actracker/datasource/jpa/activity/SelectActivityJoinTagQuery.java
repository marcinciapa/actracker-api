package ovh.equino.actracker.datasource.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.activity.ActivityEntity;
import ovh.equino.actracker.jpa.activity.ActivityEntity_;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.jpa.tag.TagEntity_;
import ovh.equino.actracker.jpa.tag.TagShareEntity;
import ovh.equino.actracker.jpa.tag.TagShareEntity_;

import java.util.Collection;
import java.util.UUID;

import static jakarta.persistence.criteria.JoinType.INNER;

final class SelectActivityJoinTagQuery extends MultiResultJpaQuery<ActivityEntity, ActivityJoinTagProjection> {

    private final PredicateBuilder predicate;
    private final Join<ActivityEntity, TagEntity> tag;

    SelectActivityJoinTagQuery(EntityManager entityManager) {
        super(entityManager);
        this.tag = root.join(ActivityEntity_.tags, INNER);
        this.predicate = new PredicateBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                criteriaBuilder.construct(
                        ActivityJoinTagProjection.class,
                        root.get(ActivityEntity_.id),
                        tag.get(TagEntity_.id)
                )
        );
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public SelectActivityJoinTagQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<ActivityEntity> getRootEntityType() {
        return ActivityEntity.class;
    }

    @Override
    protected Class<ActivityJoinTagProjection> getProjectionType() {
        return ActivityJoinTagProjection.class;
    }

    /**
     * @deprecated Sorting this entity is not supported. An attempt will throw RuntimeException.
     */
    @Override
    @Deprecated
    public JpaSortBuilder<ActivityEntity> sort() {
        throw new RuntimeException("Sorting activities joint with tags not supported");
    }

    public class PredicateBuilder extends JpaPredicateBuilder<ActivityEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate isNotDeleted() {
            return and(
                    () -> criteriaBuilder.isFalse(root.get(ActivityEntity_.deleted)),
                    () -> criteriaBuilder.isFalse(tag.get(TagEntity_.deleted))
            );
        }

        public JpaPredicate hasActivityId(UUID activityId) {
            return super.hasId(activityId);
        }

        public JpaPredicate hasActivityIdIn(Collection<UUID> activityIds) {
            return super.hasIdIn(activityIds);
        }

        public JpaPredicate isAccessibleFor(User user) {
            return or(
                    isOwner(user),
                    isTagAccessibleFor(user)
            );
        }

        private JpaPredicate isOwner(User searcher) {
            return () -> criteriaBuilder.equal(
                    root.get(ActivityEntity_.creatorId),
                    searcher.id().toString()
            );
        }

        private JpaPredicate isTagAccessibleFor(User user) {
            return or(
                    () -> criteriaBuilder.equal(tag.get(TagEntity_.creatorId), user.id().toString()),
                    isTagSharedWith(user)
            );
        }

        private JpaPredicate isTagSharedWith(User user) {
            Join<TagEntity, TagShareEntity> shares = tag.join(TagEntity_.shares, JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(shares.get(TagShareEntity_.granteeId), user.id().toString()))
                    .from(TagEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }
    }
}
