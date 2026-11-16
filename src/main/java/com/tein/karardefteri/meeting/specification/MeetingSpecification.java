
package com.tein.karardefteri.meeting.specification;

import com.tein.karardefteri.meeting.entity.Meeting;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MeetingSpecification {

    public static Specification<Meeting> filter(
            UUID projectId,
            String title,
            OffsetDateTime from,
            OffsetDateTime to,
            Boolean archived) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (projectId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("projectId"), projectId));
            }

            if (title != null && !title.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + title.trim().toLowerCase() + "%"));
            }

            if (from != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("meetingAt"), from));
            }

            if (to != null) {
                predicates.add(
                        criteriaBuilder.lessThan(
                                root.get("meetingAt"), to));
            }

            boolean archivedFilter = archived != null && archived;

            predicates.add(
                    criteriaBuilder.equal(
                            root.get("archived"), archivedFilter));

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0]));
        };
    }
}
