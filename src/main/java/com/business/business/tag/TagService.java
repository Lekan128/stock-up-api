package com.business.business.tag;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final EntityManager em;
    private final EntityViewManager evm;
    private final CriteriaBuilderFactory cbf;

    public Tag createTag(@NotBlank String tagName) {
        Tag tag = new Tag();
        tag.name = tagName;
        return tagRepository.save(tag);
    }

    //Todo: Test
    public Set<Tag> findAllByName(Set<String> tagNames){
        if (tagNames == null || tagNames.isEmpty()) {
            return Set.of();
        }

        CriteriaBuilder<Tag> tagCriteriaBuilder = cbf.create(em, Tag.class);

        return Set.copyOf(tagCriteriaBuilder.where("name").in(tagNames).getResultList());
    }

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    public Tag getTagById(String name) {
        return tagRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("Tag not found"));
    }

    public Tag updateTag(String name, @NotBlank String tagName) {
        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));
        tag.name = tagName;
        return tagRepository.save(tag);
    }

    public void deleteTag(Integer id) {
        if (!tagRepository.existsById(id)) {
            throw new IllegalArgumentException("Tag not found");
        }
        tagRepository.deleteById(id);
    }
}
