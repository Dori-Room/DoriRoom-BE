package doritos.doriroom.event.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import doritos.doriroom.diary.domain.QDiary;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.domain.EventDetailStatus;
import doritos.doriroom.event.domain.EventSortType;
import doritos.doriroom.event.domain.QEvent;
import doritos.doriroom.event.domain.QEventFavorite;
import doritos.doriroom.event.dto.request.EventItemFilterRequestDto;
import doritos.doriroom.event.dto.request.LocationFilterDto;
import doritos.doriroom.tourApi.domain.AreaGroup;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Event> findFiltered(EventItemFilterRequestDto filter, Pageable pageable) {
        QEvent event = QEvent.event;

        JPAQuery<Event> query = queryFactory
            .selectFrom(event)
            .where(
                isSuccessStatus(event),
                eqLocations(filter.locations(), event),
                eqCategoryCodes(filter.categoryCodes(), event),
                eqDateRange(filter.startDate(), filter.endDate(), event),
                eqKeyword(filter.keyword(), event)
            );

        // 정렬 적용
        if (filter.sortType() != null) {
            query.orderBy(getOrderSpecifier(filter.sortType(), event));
        } else {
            // 기본 정렬: 최신순
            query.orderBy(event.startDate.desc(), event.eventId.desc());
        }

        List<Event> results = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(event.count())
            .from(event)
            .where(query.getMetadata().getWhere())
            .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0L);
    }

    @Override
    public List<Event> findPopularEvents(int limit) {
        QEvent event = QEvent.event;
        QDiary diary = QDiary.diary;
        QEventFavorite eventFavorite = QEventFavorite.eventFavorite;

        LocalDate today = LocalDate.now();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        return queryFactory
            .select(event)
            .from(event)
            .leftJoin(diary).on(diary.eventId.eq(event.eventId)
                .and(diary.createdAt.goe(thirtyDaysAgo)))
            .leftJoin(eventFavorite).on(eventFavorite.event.eventId.eq(event.eventId)
                .and(eventFavorite.createdAt.goe(thirtyDaysAgo)))
            .where(
                isSuccessStatus(event),
                event.startDate.goe(today)
            )
            .groupBy(event.eventId)
            .orderBy(
                diary.count().multiply(3)
                    .add(eventFavorite.count().multiply(2))
                    .desc()
            )
            .limit(limit)
            .fetch();
    }

    private BooleanBuilder eqLocations(List<LocationFilterDto> locations, QEvent event) {
        if (locations == null || locations.isEmpty()) {
            return null;
        }

        BooleanBuilder builder = new BooleanBuilder();
        locations.forEach(location -> {
            BooleanBuilder condition = createLocationCondition(location, event);
            if (condition != null) {
                builder.or(condition);
            }
        });

        return builder.hasValue() ? builder : null;
    }

    private BooleanBuilder createLocationCondition(LocationFilterDto location, QEvent event) {
        BooleanBuilder builder = new BooleanBuilder();

        if (location.areaGroupCode() != null) {
            AreaGroup areaGroup = AreaGroup.fromCode(location.areaGroupCode());
            builder.and(event.areaCode.in(areaGroup.getAreaCodes()));
        }

        if (location.areaCode() != null) {
            builder.and(event.areaCode.eq(location.areaCode()));
        }

        if (location.sigunguCode() != null) {
            builder.and(event.sigungucode.eq(location.sigunguCode()));
        }

        // 생성된 조건이 있을 경우에만 반환
        return builder.hasValue() ? builder : null;
    }


    private BooleanExpression eqCategoryCodes(List<String> categoryCodes, QEvent event) {
        if(categoryCodes == null || categoryCodes.isEmpty()) return null;
        BooleanExpression cond1 = event.lclsSystm2.in(categoryCodes);
        BooleanExpression cond2 = event.lclsSystm3.in(categoryCodes);

        return cond1.or(cond2);
    }

    private BooleanBuilder eqDateRange(LocalDate startDate, LocalDate endDate, QEvent event) {
        BooleanBuilder builder = new BooleanBuilder();
        if (startDate != null) {
            builder.and(event.endDate.goe(startDate)); // 축제 종료일 >= 검색 시작일
        }
        if (endDate != null) {
            builder.and(event.startDate.loe(endDate)); // 축제 시작일 <= 검색 종료일
        }
        return builder.hasValue() ? builder : null;
    }

    private BooleanExpression eqKeyword(String keyword, QEvent event) {
        return keyword != null && !keyword.trim().isEmpty()
            ? event.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression isSuccessStatus(QEvent event) {
        return event.eventDetailStatus.eq(EventDetailStatus.SUCCESS);
    }

    private OrderSpecifier<?>[] getOrderSpecifier(EventSortType sortType, QEvent event) {
        return switch (sortType) {
            case RECOMMENDED -> {
                // 추천순: (일기개수*2 + 좋아요수) 내림차순, 시작일 내림차순
                NumberExpression<Integer> recommendationScore = event.diaryCount.multiply(2)
                    .add(event.favoriteCount)
                    .intValue();

                yield new OrderSpecifier<?>[]{recommendationScore.desc(), event.startDate.desc(), event.eventId.desc()};
            }
            case LATEST -> new OrderSpecifier<?>[]{event.startDate.desc()};
            case POPULAR -> new OrderSpecifier<?>[]{event.favoriteCount.desc(), event.startDate.desc(), event.eventId.desc()};
        };
    }
}