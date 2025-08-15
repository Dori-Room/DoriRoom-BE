package doritos.doriroom.diary.repository;

import doritos.doriroom.diary.domain.Diary;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, UUID> {

}
