package ru.practicum.categories.service;

import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService  {

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(Integer catId, CategoryDto categoryDto);

    void deleteCategory(Integer catId);

    CategoryDto getCategory(Integer catId);

    List<CategoryDto> getCategories(Integer from, Integer size);
}
