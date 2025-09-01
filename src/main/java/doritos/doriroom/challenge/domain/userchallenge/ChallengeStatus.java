package doritos.doriroom.challenge.domain.userchallenge;

public enum ChallengeStatus {
    NOT_STARTED,
    IN_PROGRESS, // 도전 중인 과제 (수동으로 도전 버튼 누르거나, 자동 집계되는 과제(횟수 누적)를 도전 중인 상태)
    WAIT_REWARD, // 조건 달성 후 보상 수령 대기 상태
    COMPLETED,  // 보상 수령 후 완전히 달성 처리된 상태
    EXPIRED // 기간이 만료된 과제
}
