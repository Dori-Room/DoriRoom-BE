package doritos.doriroom.tourApi.repository;

import doritos.doriroom.tourApi.domain.Sigungu;
import doritos.doriroom.tourApi.domain.SigunguId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SigunguRepository extends JpaRepository<Sigungu, SigunguId> {
    
    @Query("SELECT s FROM Sigungu s WHERE s.areaCode = :areaCode")
    List<Sigungu> findByAreaCode(@Param("areaCode") Integer areaCode);
    
    @Query("SELECT s FROM Sigungu s WHERE s.areaCode = :areaCode AND s.code = :code")
    SigunguId findByAreaCodeAndCode(@Param("areaCode") Integer areaCode, @Param("code") Integer code);
}
