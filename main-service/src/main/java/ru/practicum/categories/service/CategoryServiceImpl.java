package ru.practicum.categories.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.mapper.CategoryMapper;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.NotFoundException;

import java.security.InvalidParameterException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;


    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Creating a new category");
        return categoryMapper.toCategoryDto(categoryRepository.save(
                categoryMapper.toNewCategory(newCategoryDto)));
    }

    @Override
    public CategoryDto updateCategory(Integer catId, CategoryDto categoryDto) {
        Category category = getCategoryById(catId);
        category.setName(categoryDto.getName());
        log.info("Category with ID={} was updated", catId);
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Integer catId) {
        Category category = getCategoryById(catId);
        if (eventRepository.findByCategory(category).isPresent()) {
            throw new InvalidParameterException("Category is related to event");
        }
        categoryRepository.deleteById(catId);
        log.info("Category with ID={} was deleted", catId);
    }

    @Override
    public CategoryDto getCategory(Integer catId) {
        Category category = getCategoryById(catId);
        log.info("Получение категории с ID={}", catId);
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable page = PageRequest.of(from, size);

        List<Category> categories = categoryRepository.findAll(page).getContent();
        if (categories.isEmpty()) {
            return List.of();
        } else {
            return categories.stream()
                    .map(categoryMapper::toCategoryDto)
                    .toList();
        }
    }

    private Category getCategoryById(Integer catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", catId)));
    }
}
