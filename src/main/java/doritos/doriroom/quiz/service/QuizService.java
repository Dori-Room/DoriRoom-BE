package doritos.doriroom.quiz.service;

import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.domain.userchallenge.ChallengeStatus;
import doritos.doriroom.challenge.domain.userchallenge.UserChallenge;
import doritos.doriroom.challenge.dto.ChallengeRewardDto;
import doritos.doriroom.challenge.exception.ChallengeNotFoundException;
import doritos.doriroom.challenge.exception.ChallengeStatusException;
import doritos.doriroom.challenge.repository.ChallengeRepository;
import doritos.doriroom.challenge.repository.UserChallengeRepository;
import doritos.doriroom.quiz.domain.Question;
import doritos.doriroom.quiz.domain.Quiz;
import doritos.doriroom.quiz.dto.requset.QuestionSubmitRequestDto;
import doritos.doriroom.quiz.dto.response.QuestionSubmitResponseDto;
import doritos.doriroom.quiz.dto.response.QuizCompleteResponseDto;
import doritos.doriroom.quiz.dto.response.QuizResponseDto;
import doritos.doriroom.quiz.exception.QuizNotFoundException;
import doritos.doriroom.quiz.repository.QuestionRepository;
import doritos.doriroom.quiz.repository.QuizRepository;
import doritos.doriroom.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final QuestionRepository questionRepository;


    @Transactional
    public QuizResponseDto getQuiz(User user, Long challengeId) { // 퀴즈와 문제 목록 조회
        // 도전과제를 불러옴
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException());

        // 유저의 해당 도전과제 진행 상태를 조회, 없다면 새로 생성
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseGet(() -> userChallengeRepository.save(
                        UserChallenge.builder()
                                .user(user)
                                .challenge(challenge)
                                .status(ChallengeStatus.NOT_STARTED)
                                .build()
                ));

        // 이미 진행 중인 상태가 아니라면 예외 처리
        if (userChallenge.getStatus() != ChallengeStatus.NOT_STARTED &&
                userChallenge.getStatus() != ChallengeStatus.IN_PROGRESS) {
            throw new ChallengeStatusException("이미 완료했거나 보상 대기 중인 퀴즈입니다.");
        }

        // 상태가 NOT_STARTED이면 IN_PROGRESS로 변경
        if (userChallenge.getStatus() == ChallengeStatus.NOT_STARTED) {
            userChallenge.setStatus(ChallengeStatus.IN_PROGRESS);
        }

        // 해당 도전과제의 퀴즈를 불러옴
        Quiz quiz = quizRepository.findWithQuestionsByChallengeId(challengeId)
                .orElseThrow(() -> new QuizNotFoundException("ID " + challengeId + "에 해당하는 퀴즈가 없습니다."));

        return QuizResponseDto.from(quiz);
    }

    // 유저가 제출한 문제의 답 확인 및 해설 반환
    public QuestionSubmitResponseDto submitQuestionAnswer(User user, QuestionSubmitRequestDto request){
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new QuizNotFoundException("해당 문제를 찾을 수 없습니다."));

        boolean isCorrect = question.getCorrectAnswer() == request.submittedAnswer(); // 채점 boolean

        return QuestionSubmitResponseDto.of(question, isCorrect);
    }

    @Transactional
    public QuizCompleteResponseDto completeQuiz(User user, Long challengeId) { // 해당 도전과제를 보상 대기 처리
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeNotFoundException("ID " + challengeId + "에 해당하는 도전과제가 없습니다."));

        // 유저의 도전과제에 대한 상태 조회 없으면 예외
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new ChallengeStatusException("아직 시작하지 않은 도전과제입니다."));

        // 도전 중인지 확인 아닌 경우에 예외로 처리
        if (userChallenge.getStatus() != ChallengeStatus.IN_PROGRESS) {
            throw new ChallengeStatusException("이미 완료했거나 보상 대기 중인 과제입니다.");
        }

        //  보상 대기 상태로 변경
        userChallenge.setStatus(ChallengeStatus.WAIT_REWARD);


        // 도전과제의 보상 정보를 dto로 변환
        List<ChallengeRewardDto> rewards = challenge.getRewards().stream()
                .map(ChallengeRewardDto::from) // 각 ChallengeReward를 ChallengeRewardDto로 변환
                .collect(Collectors.toList());

        return new QuizCompleteResponseDto(true, rewards);
    }
}

