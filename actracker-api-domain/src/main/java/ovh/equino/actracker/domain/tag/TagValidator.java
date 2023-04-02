package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.EntityValidator;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

final class TagValidator extends EntityValidator<Tag> {

    private final Tag tag;

    TagValidator(Tag tag) {
        this.tag = tag;
    }

    @Override
    protected List<String> collectValidationErrors() {
        List<String> validationErrors = new LinkedList<>();

        if (isBlank(tag.name())) {
            validationErrors.add("Name is empty");
        }

        return validationErrors;
    }

    @Override
    protected Class<Tag> entityType() {
        return Tag.class;
    }
}
