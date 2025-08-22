package doritos.doriroom.quiz.service;

import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.domain.challenge.ChallengeReward;
import doritos.doriroom.challenge.domain.userchallenge.ChallengeStatus;
import doritos.doriroom.challenge.domain.userchallenge.UserChallenge;
import doritos.doriroom.challenge.dto.ChallengeRewardDto;
import doritos.doriroom.challenge.excetion.ChallengeNotFoundException;
import doritos.doriroom.challenge.repository.ChallengeRepository;
import doritos.doriroom.challenge.repository.UserChallengeRepository;
import doritos.doriroom.quiz.domain.Quiz;
import doritos.doriroom.quiz.dto.requset.QuizCompleteRequestDto;
import doritos.doriroom.quiz.dto.response.QuizCompleteResponseDto;
import doritos.doriroom.quiz.dto.response.QuizResponseDto;
import doritos.doriroom.quiz.exception.QuizNotFoundException;
import doritos.doriroom.quiz.exception.QuizStatusException;
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

        // 상태가 NOT_STARTED이면 IN_PROGRESS로 변경
        if (userChallenge.getStatus() == ChallengeStatus.NOT_STARTED) {
            userChallenge.setStatus(ChallengeStatus.IN_PROGRESS);
        }

        // 해당 도전과제의 퀴즈를 불러옴
        Quiz quiz = quizRepository.findWithQuestionsByChallengeId(challengeId)
                .orElseThrow(() -> new QuizNotFoundException("ID " + challengeId + "에 해당하는 퀴즈가 없습니다."));

        return QuizResponseDto.from(quiz);
    }

    @Transactional
    public QuizCompleteResponseDto completeQuiz(User user, QuizCompleteRequestDto request) { // 해당 도전과제를 완료 처리

        Challenge challenge = challengeRepository.findById(request.challengeId())
                .orElseThrow(() -> new ChallengeNotFoundException("ID " + request.challengeId() + "에 해당하는 도전과제가 없습니다."));

        // 유저의 도전과제에 대한 상태 조회 없으면 예외 (퀴즈 조회 시 IN_PROGRESS로 설정되므로)
        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new QuizStatusException("아직 시작하지 않은 퀴즈입니다."));

        // 도전 중인지 확인 아닌 경우에 예외로 처리
        if (userChallenge.getStatus() != ChallengeStatus.IN_PROGRESS) {
            throw new QuizStatusException("이미 완료했거나 보상 대기 중인 과제입니다.");
        }

        //  보상 대기 상태로 변경
        userChallenge.setStatus(ChallengeStatus.WAIT_REWARD);
//        userChallengeRepository.save(userChallenge);


        // 도전과제의 보상 정보를 dto로 변환
        List<ChallengeRewardDto> rewards = challenge.getRewards().stream()
                .map(ChallengeRewardDto::from) // 각 ChallengeReward를 ChallengeRewardDto로 변환
                .collect(Collectors.toList());

        return new QuizCompleteResponseDto(true, rewards);
    }
}

