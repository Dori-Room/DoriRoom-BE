package doritos.doriroom.event.domain;

public enum EventDetailStatus {
    SUCCESS,                    // 성공적으로 업데이트됨
    FAILED,                     // 업데이트 실패
    PENDING,                    // 초기 상태
    DELETED_FROM_API           // TourAPI에서 축제가 삭제됨
}
