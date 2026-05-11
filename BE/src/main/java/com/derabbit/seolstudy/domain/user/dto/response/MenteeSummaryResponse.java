package com.derabbit.seolstudy.domain.user.dto.response;

import java.time.LocalDate;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenteeSummaryResponse {

    private LocalDate from;
    private LocalDate to;
    private Map<String, SubjectSummaryResponse> subjects;

    public static MenteeSummaryResponse of(LocalDate from, LocalDate to, Map<String, SubjectSummaryResponse> subjects) {
        return MenteeSummaryResponse.builder()
                .from(from)
                .to(to)
                .subjects(subjects)
                .build();
    }
}
