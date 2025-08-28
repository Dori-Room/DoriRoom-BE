package doritos.doriroom.quiz.domain;

import doritos.doriroom.challenge.domain.challenge.Challenge;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Builder
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false, unique = true)
    private Challenge challenge; // 도전과제와 1대1 매핑

    private String title; // 예: "경상도 지역 퀴즈"

    // 해당 퀴즈에 포함될 문제 목록
    @NotEmpty
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence asc") // 문제 순서로 정렬
    private List<Question> questions = new ArrayList<>();

}
