package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetRepository;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;
import java.util.UUID;

class JpaTagSetRepository extends JpaDAO implements TagSetRepository {

    JpaTagSetRepository(EntityManager entityManager) {
        super(entityManager);
    }

    private final TagSetMapper mapper = new TagSetMapper();

    @Override
    public void add(TagSetDto tagSet) {
        TagSetEntity tagSetEntity = mapper.toEntity(tagSet);
        entityManager.persist(tagSetEntity);
    }

    @Override
    public void update(UUID tagSetId, TagSetDto tagSet) {
        TagSetEntity tagSetEntity = mapper.toEntity(tagSet);
        tagSetEntity.id = tagSetId.toString();
        entityManager.merge(tagSetEntity);
    }

    @Override
    public Optional<TagSetDto> findById(UUID tagSetId) {
        TagSetQueryBuilder queryBuilder = new TagSetQueryBuilder(entityManager);

        CriteriaQuery<TagSetEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.hasId(tagSetId),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<TagSetEntity> typedQuery = entityManager.createQuery(query);

        return typedQuery.getResultList().stream()
                .findFirst()
                .map(mapper::toDto);
    }
}
