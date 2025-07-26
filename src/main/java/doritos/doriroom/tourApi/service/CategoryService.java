package doritos.doriroom.tourApi.service;

import doritos.doriroom.tourApi.domain.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryService {
    public List<Category> getAllCategories() {
        return List.of(Category.values());
    }
} 