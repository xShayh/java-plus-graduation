package ru.practicum.categories.service;

import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.categories.NewCategoryDto;

import java.util.List;

public interface CategoryService  {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

    void deleteCategory(Long catId);

    CategoryDto getCategory(Long catId);

    List<CategoryDto> getCategories(Integer from, Integer size);
}
