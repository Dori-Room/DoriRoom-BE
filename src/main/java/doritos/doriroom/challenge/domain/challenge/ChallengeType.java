package doritos.doriroom.challenge.domain.challenge;

public enum ChallengeType {
    // 일반 과제 예시  (자동으로 횟수 누적 집계하여 달성 가능)
    WRITE_DIARY,        // 일기 작성
    VISIT_NEIGHBOR,     // 이웃 집 방문
    REACH_VISIT_COUNT,      // 방 조회수 N번 달성
    REACH_NEIGHBOR_COUNT, // 이웃 N명 달성 (내가 추가한 이웃, 팔로잉)
    REACH_FOLLOWER_COUNT, // 나를 추가한 이웃 N명 달성 (팔로워)
    REACH_ROOM_COUNT,    // 방 좋아요 수 N개 달성
    COLLECT_ITEM,       // 아이템 수집


    // 지역 과제 (도전 버튼 클릭으로 수행)
    VISIT_EVENT,     // 특정 축제 방문
    REGIONAL_QUIZ, // 지역 퀴즈 풀기
}
