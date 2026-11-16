package com.tein.karardefteri.task.repository;

import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.entity.TaskPriority;
import com.tein.karardefteri.task.entity.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> withFilters(
            UUID projectId,
            UUID assigneeId,
            TaskStatus status,
            TaskPriority priority,
            OffsetDateTime dueFrom,
            OffsetDateTime dueTo,
            Boolean overdue,
            OffsetDateTime todayStart,
            UUID currentUserId
    ) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (assigneeId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("assigneeId"), assigneeId
                        )
                );
            }

            if (currentUserId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("assigneeId"), currentUserId
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"), status
                        )
                );
            }

            if (priority != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("priority"), priority
                        )
                );
            }

            if (dueFrom != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("dueDate"), dueFrom
                        )
                );
            }

            if (dueTo != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("dueDate"), dueTo
                        )
                );
            }

            if (overdue != null && todayStart != null) {
                if (overdue) {
                    predicates.add(
                            criteriaBuilder.and(
                                    criteriaBuilder.lessThan(
                                            root.get("dueDate"), todayStart
                                    ),
                                    criteriaBuilder.notEqual(
                                            root.get("status"), TaskStatus.DONE
                                    )
                            )
                    );
                } else {
                    predicates.add(
                            criteriaBuilder.or(
                                    criteriaBuilder.isNull(root.get("dueDate")),
                                    criteriaBuilder.greaterThanOrEqualTo(
                                            root.get("dueDate"), todayStart
                                    ),
                                    criteriaBuilder.equal(
                                            root.get("status"), TaskStatus.DONE
                                    )
                            )
                    );
                }
            }

            if (projectId != null) {
                var meetingSubquery = query.subquery(UUID.class);
                var meetingRoot = meetingSubquery.from(Meeting.class);

                meetingSubquery.select(meetingRoot.get("id"))
                        .where(
                                criteriaBuilder.equal(
                                        meetingRoot.get("projectId"), projectId
                                )
                        );

                predicates.add(
                        root.get("meetingId").in(meetingSubquery)
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}