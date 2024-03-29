package ovh.equino.actracker.datasource.jpa.tag;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import ovh.equino.actracker.datasource.jpa.JpaPredicate;
import ovh.equino.actracker.datasource.jpa.JpaPredicateBuilder;
import ovh.equino.actracker.datasource.jpa.JpaSortBuilder;
import ovh.equino.actracker.datasource.jpa.MultiResultJpaQuery;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.tag.TagEntity;
import ovh.equino.actracker.jpa.tag.TagEntity_;
import ovh.equino.actracker.jpa.tag.TagShareEntity;
import ovh.equino.actracker.jpa.tag.TagShareEntity_;

final class SelectTagsQuery extends MultiResultJpaQuery<TagEntity, TagProjection> {

    private final PredicateBuilder predicate;
    private final SortBuilder sort;

    SelectTagsQuery(EntityManager entityManager) {
        super(entityManager);
        this.predicate = new PredicateBuilder();
        this.sort = new SortBuilder();
    }

    @Override
    protected void initProjection() {
        query.select(
                        criteriaBuilder.construct(
                                TagProjection.class,
                                root.get(TagEntity_.id),
                                root.get(TagEntity_.creatorId),
                                root.get(TagEntity_.name),
                                root.get(TagEntity_.deleted)
                        )
                )
                .distinct(true);
    }

    @Override
    public PredicateBuilder predicate() {
        return predicate;
    }

    @Override
    public JpaSortBuilder<TagEntity> sort() {
        return sort;
    }

    @Override
    public SelectTagsQuery where(JpaPredicate predicate) {
        super.where(predicate);
        return this;
    }

    @Override
    protected Class<TagEntity> getRootEntityType() {
        return TagEntity.class;
    }

    @Override
    protected Class<TagProjection> getProjectionType() {
        return TagProjection.class;
    }

    public final class PredicateBuilder extends JpaPredicateBuilder<TagEntity> {
        private PredicateBuilder() {
            super(criteriaBuilder, root);
        }

        public JpaPredicate isNotDeleted() {
            return () -> criteriaBuilder.isFalse(root.get(TagEntity_.deleted));
        }

        JpaPredicate matchesTerm(String term) {
            return super.matchesTerm(term, root.get(TagEntity_.name));
        }

        public JpaPredicate isAccessibleFor(User searcher) {
            return or(
                    isOwner(searcher),
                    isGrantee(searcher)
            );
        }

        private JpaPredicate isOwner(User searcher) {
            return () -> criteriaBuilder.equal(
                    root.get(TagEntity_.creatorId),
                    searcher.id().toString()
            );
        }

        private JpaPredicate isGrantee(User user) {
            Join<TagEntity, TagShareEntity> sharedTag = root.join(TagEntity_.shares, JoinType.LEFT);
            Subquery<Long> subQuery = query.subquery(Long.class);
            subQuery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.equal(sharedTag.get(TagShareEntity_.granteeId), user.id().toString()))
                    .from(TagEntity.class);
            return () -> criteriaBuilder.exists(subQuery);
        }
    }

    public final class SortBuilder extends JpaSortBuilder<TagEntity> {
        private SortBuilder() {
            super(criteriaBuilder, root);
        }
    }
}
