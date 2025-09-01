package doritos.doriroom.quiz.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
  name = "questions",
  uniqueConstraints = {
    @UniqueConstraint(name = "uk_questions_quiz_seq", columnNames = {"quiz_id", "sequence"})
  },
  indexes = {
     @Index(name = "idx_questions_quiz", columnList = "quiz_id")
  }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz; // 퀴즈와 다대일 매핑함

    @Column(nullable = false)
    private int sequence; // 퀴즈에서 몇 번 문제에 해당하는지 지정 (1번 문제, 2번 ..)

    @Column(nullable = false, length = 500)
    private String content; // 문제 내용

    @Column(nullable = false) // 문제 답안 (OX퀴즈의 경우 option1을 O, 2를 X로 지정하여 사용)
    private String option1;
    @Column(nullable = false)
    private String option2;

    // 4지선다일 경우 사용, 2지선다면 null
    private String option3;
    private String option4;

    @Column(nullable = false)
    private byte correctAnswer; // 정답 번호 (1, 2, 3, 4 중)

    @Column(nullable = false, length = 1000)
    private String commentary; // 문제 해설 내용
}
