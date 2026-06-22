package com.example.mentorisebackend.service.tutor;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.dto.tutor.TutorCardDto;
import com.example.mentorisebackend.dto.tutor.TutorSwipeResponse;
import com.example.mentorisebackend.enums.ScopeType;
import com.example.mentorisebackend.pagination.SwipeCursor;
import com.example.mentorisebackend.repository.*;
import com.example.mentorisebackend.repository.projection.tutor.RankedTutorRow;
import com.example.mentorisebackend.repository.projection.tutor.TutorCoreRow;
import com.example.mentorisebackend.repository.projection.tutor.TutorScopeWithNameRow;
import com.example.mentorisebackend.service.user.SwipeUserContextService;
import com.example.mentorisebackend.service.user.UserScopeSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TutorSwipeService {

    private final CurrentUser currentUser;
    private final TutorMatchRepository matchRepository;
    private final TutorSwipeDetailsRepository detailsRepository;
    private final SwipeUserContextService swipeUserContextService;
    private final TutorMatchReasonService tutorMatchReasonService;

    public TutorSwipeResponse getSwipeTutors(Integer limitParam, String cursorToken, List<Long> excludeIds) {
        int limit = normalizeLimit(limitParam);

        Long userId  = currentUser.getUserId();
        Long majorId = swipeUserContextService.getMajorId(userId);

        UserScopeSnapshot prefs    = swipeUserContextService.getScopeSnapshot(userId);
        ScopeType         mode     = prefs.mode();
        List<Integer>     years    = prefs.years();
        List<Long>        courseIds = prefs.courseIds();

        SwipeCursor cursor = SwipeCursor.decode(cursorToken);

        List<Long> effectiveExclude = new ArrayList<>();
        if (excludeIds != null) {
            effectiveExclude.addAll(excludeIds);
        }
        effectiveExclude.add(userId);
        effectiveExclude = effectiveExclude.stream().distinct().toList();

        int excludeEmpty = effectiveExclude.isEmpty() ? 1 : 0;
        int fetch        = limit + 1;

        List<RankedTutorRow> ranked = runPrimary(
                mode, majorId, years, courseIds, cursor, effectiveExclude, excludeEmpty, fetch);

        boolean cycleResetSuggested = false;

        if (ranked.isEmpty()) {
            cycleResetSuggested = true;
            SwipeCursor restart = SwipeCursor.start();
            ranked = runPrimary(
                    mode, majorId, years, courseIds, restart, effectiveExclude, excludeEmpty, fetch);
        }

        boolean              hasMore = ranked.size() > limit;
        List<RankedTutorRow> page    = ranked.stream().limit(limit).toList();
        // OLD: List<TutorCardDto>   items   = mapToTutorCards(page);
        List<TutorCardDto> items = mapToTutorCards(page);
        String majorName = items.isEmpty() ? "" : items.get(0).majorName();
        items = tutorMatchReasonService.enrich(items, prefs, majorName);

        String nextCursor = null;
        if (hasMore && !page.isEmpty()) {
            RankedTutorRow last = page.get(page.size() - 1);
            nextCursor = new SwipeCursor(last.getRank(), last.getId()).encode();
        }

        boolean suggested = cycleResetSuggested || !hasMore;
        return new TutorSwipeResponse(items, nextCursor, hasMore, suggested);
    }

    private List<TutorCardDto> mapToTutorCards(List<RankedTutorRow> page) {
        List<Long> ids = page.stream().map(RankedTutorRow::getId).toList();
        if (ids.isEmpty()) return Collections.emptyList();

        Map<Long, TutorCoreRow> coreById = detailsRepository.findCoreByTutorIds(ids)
                .stream()
                .collect(Collectors.toMap(TutorCoreRow::getTutorId, r -> r));

        List<TutorScopeWithNameRow> scopeRows =
                detailsRepository.findScopesWithNamesByTutorIds(ids);

        Map<Long, Set<Integer>> yearsByTutor      = new HashMap<>();
        Map<Long, Set<String>>  courseNamesByTutor = new HashMap<>();

        for (TutorScopeWithNameRow r : scopeRows) {
            Long   tutorId = r.getTutorId();
            String type    = r.getScopeType();

            if ("YEAR".equals(type) && r.getYearNumber() != null) {
                yearsByTutor.computeIfAbsent(tutorId, k -> new HashSet<>()).add(r.getYearNumber());
            }
            if ("COURSE".equals(type) && r.getCourseName() != null) {
                courseNamesByTutor.computeIfAbsent(tutorId, k -> new HashSet<>()).add(r.getCourseName());
            }
        }

        List<TutorCardDto> result = new ArrayList<>();
        for (Long id : ids) {
            TutorCoreRow core = coreById.get(id);
            if (core == null) continue;

            List<Integer> tutorYears = yearsByTutor.getOrDefault(id, Set.of())
                    .stream().sorted().toList();

            List<String> courseNames = courseNamesByTutor.getOrDefault(id, Set.of())
                    .stream().sorted().toList();

            // OLD: result.add(new TutorCardDto(
            // OLD:         id,
            // OLD:         core.getFullName(),
            // OLD:         core.getMajorName(),
            // OLD:         core.getBio(),
            // OLD:         core.getTutorImageUrl(),
            // OLD:         tutorYears,
            // OLD:         courseNames,
            // OLD:         core.isAlumni()
            // OLD: ));
            result.add(new TutorCardDto(
                    id,
                    core.getFullName(),
                    core.getMajorName(),
                    core.getBio(),
                    core.getTutorImageUrl(),
                    tutorYears,
                    courseNames,
                    core.isAlumni(),
                    null
            ));
        }
        return result;
    }

    private int normalizeLimit(Integer limitParam) {
        if (limitParam == null) return 10;
        if (limitParam < 1)    return 10;
        if (limitParam > 30)   return 30;
        return limitParam;
    }

    private List<RankedTutorRow> runPrimary(
            ScopeType mode, Long majorId, List<Integer> years, List<Long> courseIds,
            SwipeCursor cursor, List<Long> exclude, int excludeEmpty, int fetch
    ) {
        if (mode == ScopeType.COURSE) {
            int yearsEmpty   = (years != null && !years.isEmpty()) ? 0 : 1;
            List<Integer> safeYears = (years != null && !years.isEmpty())
                    ? years : Collections.singletonList(-1);
            return matchRepository.findCourseUnified(
                    majorId, courseIds, safeYears, yearsEmpty,
                    exclude, excludeEmpty, cursor.rank(), cursor.lastId(), fetch);
        }
        if (mode == ScopeType.YEAR) {
            return matchRepository.findYearUnified(
                    majorId, years, exclude, excludeEmpty, cursor.rank(), cursor.lastId(), fetch);
        }
        return matchRepository.findMajorUnified(
                majorId, exclude, excludeEmpty, cursor.rank(), cursor.lastId(), fetch);
    }
}
