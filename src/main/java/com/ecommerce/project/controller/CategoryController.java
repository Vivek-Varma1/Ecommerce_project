package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController  {
private CategoryService categoryService;

@Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(@RequestParam(name = "pageNumber" ,defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
                                                             @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
                                                             @RequestParam(name = "sortBy",defaultValue = AppConstants.SORT_CATEGORIES_BY,required = false)String sortBy,
                                                             @RequestParam(name = "sortOrder",defaultValue = AppConstants.SORT_DIR,required = false)String sortOrder){
        CategoryResponse categorieRespose= categoryService.getAllCategories(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(categorieRespose,HttpStatus.OK);
    }

    @PostMapping("/public/addCategory")
    public ResponseEntity<CategoryDTO> add(@Valid @RequestBody CategoryDTO categoryDTO){

        CategoryDTO savedCategory=  categoryService.addCategory(categoryDTO);
        return new ResponseEntity<>(savedCategory,HttpStatus.OK);
    }
    @DeleteMapping("/admin/deleteCategory/{id}")
    public ResponseEntity<CategoryDTO> delete(@PathVariable Long id){

        CategoryDTO deletedCategory = categoryService.deleteCategory(id);
        return new ResponseEntity<>(deletedCategory, HttpStatus.OK);
    }
    @PutMapping("/public/updateCategory/{id}")
    public ResponseEntity<CategoryDTO> update(@RequestBody CategoryDTO categoryDTO,
                                            @PathVariable Long id){
      CategoryDTO savedCategoryDTO =categoryService.updateCategory(categoryDTO,id);
        return new ResponseEntity<>(savedCategoryDTO,HttpStatus.OK);
    }


}
