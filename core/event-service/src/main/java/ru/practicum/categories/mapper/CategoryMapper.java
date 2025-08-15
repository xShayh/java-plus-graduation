package ru.practicum.categories.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.categories.model.Category;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.categories.NewCategoryDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    CategoryDto toCategoryDto(Category category);

    Category toNewCategory(NewCategoryDto newCategoryDto);

    Category toCategory(CategoryDto categoryDto);
}
