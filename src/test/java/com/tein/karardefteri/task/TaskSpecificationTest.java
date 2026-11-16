package com.tein.karardefteri.task;

import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.entity.TaskStatus;
import com.tein.karardefteri.task.repository.TaskSpecification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskSpecificationTest {

    @Test
    @SuppressWarnings("unchecked")
    void overdueTrue_shouldRequirePastDueDateAndNotDoneStatus() {

        Root<Task> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<OffsetDateTime> dueDatePath = mock(Path.class);
        Path<TaskStatus> statusPath = mock(Path.class);

        Predicate pastDuePredicate = mock(Predicate.class);
        Predicate notDonePredicate = mock(Predicate.class);
        Predicate overduePredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        OffsetDateTime todayStart =
                OffsetDateTime.parse("2026-11-05T00:00:00+03:00");

        when(root.<OffsetDateTime>get("dueDate"))
                .thenReturn(dueDatePath);

        when(root.<TaskStatus>get("status"))
                .thenReturn(statusPath);

        when(criteriaBuilder.lessThan(dueDatePath, todayStart))
                .thenReturn(pastDuePredicate);

        when(criteriaBuilder.notEqual(statusPath, TaskStatus.DONE))
                .thenReturn(notDonePredicate);

        when(criteriaBuilder.and(pastDuePredicate, notDonePredicate))
                .thenReturn(overduePredicate);

        when(criteriaBuilder.and(overduePredicate))
                .thenReturn(finalPredicate);

        Predicate result = TaskSpecification.withFilters(
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                todayStart,
                null
        ).toPredicate(root, query, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).lessThan(dueDatePath, todayStart);
        verify(criteriaBuilder).notEqual(statusPath, TaskStatus.DONE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void overdueFalse_shouldIncludeTasksWithoutDueDateTodayOrDone() {

        Root<Task> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<OffsetDateTime> dueDatePath = mock(Path.class);
        Path<TaskStatus> statusPath = mock(Path.class);

        Predicate noDueDate = mock(Predicate.class);
        Predicate dueTodayOrLater = mock(Predicate.class);
        Predicate completed = mock(Predicate.class);
        Predicate notOverdue = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        OffsetDateTime todayStart =
                OffsetDateTime.parse("2026-11-05T00:00:00+03:00");

        when(root.<OffsetDateTime>get("dueDate"))
                .thenReturn(dueDatePath);

        when(root.<TaskStatus>get("status"))
                .thenReturn(statusPath);

        when(criteriaBuilder.isNull(dueDatePath))
                .thenReturn(noDueDate);

        when(criteriaBuilder.greaterThanOrEqualTo(
                dueDatePath, todayStart
        )).thenReturn(dueTodayOrLater);

        when(criteriaBuilder.equal(statusPath, TaskStatus.DONE))
                .thenReturn(completed);

        when(criteriaBuilder.or(
                noDueDate, dueTodayOrLater, completed
        )).thenReturn(notOverdue);

        when(criteriaBuilder.and(notOverdue))
                .thenReturn(finalPredicate);

        Predicate result = TaskSpecification.withFilters(
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                todayStart,
                null
        ).toPredicate(root, query, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).isNull(dueDatePath);
        verify(criteriaBuilder).greaterThanOrEqualTo(
                dueDatePath, todayStart
        );
        verify(criteriaBuilder).equal(statusPath, TaskStatus.DONE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void dueDateFilters_shouldIncludeBothBoundaryDates() {

        Root<Task> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);

        Path<OffsetDateTime> dueDatePath = mock(Path.class);

        Predicate fromPredicate = mock(Predicate.class);
        Predicate toPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        OffsetDateTime dueFrom =
                OffsetDateTime.parse("2026-11-01T00:00:00+03:00");

        OffsetDateTime dueTo =
                OffsetDateTime.parse("2026-11-30T23:59:59+03:00");

        when(root.<OffsetDateTime>get("dueDate"))
                .thenReturn(dueDatePath);

        when(criteriaBuilder.greaterThanOrEqualTo(
                dueDatePath, dueFrom
        )).thenReturn(fromPredicate);

        when(criteriaBuilder.lessThanOrEqualTo(
                dueDatePath, dueTo
        )).thenReturn(toPredicate);

        // Varargs Predicate dizisini doğru şekilde eşleştirir.
        when(criteriaBuilder.and(any(Predicate[].class)))
                .thenReturn(finalPredicate);

        Predicate result = TaskSpecification.withFilters(
                null,
                null,
                null,
                null,
                dueFrom,
                dueTo,
                null,
                null,
                null
        ).toPredicate(root, query, criteriaBuilder);

        assertSame(finalPredicate, result);

        verify(criteriaBuilder).greaterThanOrEqualTo(
                dueDatePath, dueFrom
        );

        verify(criteriaBuilder).lessThanOrEqualTo(
                dueDatePath, dueTo
        );
    }
}