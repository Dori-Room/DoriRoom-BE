package doritos.doriroom.event.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.domain.QEvent;
import doritos.doriroom.event.dto.request.EventItemFilterRequestDto;
import java.time.LocalDate;
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

        List<Event> results = queryFactory
            .selectFrom(event)
            .where(
                eqAreaCode(filter.areaCode(), event),
                eqSigunguCodes(filter.sigunguCodes(), event),
                eqCategoryCodes(filter.categoryCodes(), event),
                eqStartDate(filter.startDate(), event),
                eqEndDate(filter.endDate(), event),
                eqKeyword(filter.keyword(), event)
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(event.startDate.asc())
            .fetch();

        Long total = queryFactory
            .select(event.count())
            .from(event)
            .where(
                eqAreaCode(filter.areaCode(), event),
                eqSigunguCodes(filter.sigunguCodes(), event),
                eqCategoryCodes(filter.categoryCodes(), event),
                eqStartDate(filter.startDate(), event),
                eqEndDate(filter.endDate(), event),
                eqKeyword(filter.keyword(), event)
            )
            .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0L);
    }

    private BooleanExpression eqAreaCode(Integer areaCode, QEvent event) {
        return areaCode != null ? event.areaCode.eq(areaCode) : null;
    }

    private BooleanExpression eqSigunguCodes(List<Integer> sigunguCodes, QEvent event) {
        return sigunguCodes != null && !sigunguCodes.isEmpty()
            ? event.sigungucode.in(sigunguCodes) : null;
    }

    private BooleanExpression eqCategoryCodes(List<String> categoryCodes, QEvent event) {
        if(categoryCodes == null || categoryCodes.isEmpty()) return null;
        BooleanExpression cond1 = event.lclsSystm2.in(categoryCodes);
        BooleanExpression cond2 = event.lclsSystm3.in(categoryCodes);

        return cond1.or(cond2);
    }

    private BooleanExpression eqStartDate(LocalDate startDate, QEvent event) {
        return startDate != null ? event.endDate.goe(startDate) : null;
    }

    private BooleanExpression eqEndDate(LocalDate endDate, QEvent event) {
        return endDate != null ? event.startDate.loe(endDate) : null;
    }

    private BooleanExpression eqKeyword(String keyword, QEvent event) {
        return keyword != null && !keyword.trim().isEmpty()
            ? event.title.containsIgnoreCase(keyword) : null;
    }
}
