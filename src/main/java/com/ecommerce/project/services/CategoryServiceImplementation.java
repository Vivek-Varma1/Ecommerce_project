package com.ecommerce.project.services;

import com.ecommerce.project.customExceptionHandler.ApiException;
import com.ecommerce.project.customExceptionHandler.ResourseNorFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@Service
public class CategoryServiceImplementation implements CategoryService{
//    private List<Category> categoryList=new ArrayList<>();
//    private long index=1L;
    @Autowired
    private CategoryRepository repo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {
//        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
//                ?Sort.by(sortBy).ascending()
//                            :Sort.by(sortBy).descending();
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(Sort.Order.asc(sortBy).ignoreCase())
                : Sort.by(Sort.Order.desc(sortBy).ignoreCase());



        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Category> categoryPage=repo.findAll(pageDetails);
        List<Category>  savedCategories=categoryPage.getContent();
        if (savedCategories.isEmpty())
            throw new ApiException("there are no Categories to fetch");
    List<CategoryDTO> categoryDTOS=savedCategories.stream()
            .map(category -> modelMapper.map(category,CategoryDTO.class))
            .toList();
    CategoryResponse categoryResponse=new CategoryResponse();

    categoryResponse.setContent(categoryDTOS);
    categoryResponse.setPageNumber(categoryPage.getNumber());
    categoryResponse.setPageSize(categoryPage.getSize());
    categoryResponse.setTotalPages(categoryPage.getTotalPages());
    categoryResponse.setTotalElements(categoryPage.getTotalElements());
    categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDTO addCategory(@RequestBody CategoryDTO categoryDTO) {
    Category category=modelMapper.map(categoryDTO,Category.class);
        Category categoryFromDb=repo.findByCategoryName(category.getCategoryName());
//        category.setCategoryId(index++);
        if (categoryFromDb!=null)
                throw new ApiException("Category with the name '"+ categoryDTO.getCategoryName() +"' Already exists ");

       Category savedCategory= repo.save(category);
        return modelMapper.map(savedCategory,CategoryDTO.class);
    }

    @Override
    public CategoryDTO deleteCategory(Long id){
//        Category category=categoryList.stream()
//                        .filter(c->c.getCategoryId().equals(id))    //if you want to use .equals()
//                                                                            // we have to make sure that the
//                                                                             // two variables are must be of same type
//                                .findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resource not found ...! "));
//        if(category==null){
//            return "Category not found!";
//        }
//        categoryList.remove(category);
//        repo.deleteById(id);
//        return "Category with Id : "+id+" is deleted Successful";
        Category deletedCategory=repo.findById(id)
                .orElseThrow(()->new ResourseNorFoundException("Category","categoryId",id));

        CategoryDTO categoryDTO=modelMapper.map(deletedCategory,CategoryDTO.class);
        repo.delete(deletedCategory);
        return modelMapper.map(deletedCategory,CategoryDTO.class);



    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long id) {

            Category savedCategory=repo.findById(id)
                .orElseThrow(()->new ResourseNorFoundException("Category","categoryId",id));

        Category category=modelMapper.map(categoryDTO,Category.class);
        category.setCategoryId(id);
        savedCategory=repo.save(category);
        return modelMapper.map(savedCategory,CategoryDTO.class);

//        Optional<Category> optionalCategory=categoryList.stream()
//                .filter(c->c.getCategoryId().equals(id))
//                .findFirst();
//
//        if(optionalCategory.isPresent()){
//            Category existingCatogory=optionalCategory.get();
//            existingCatogory.setCategoryName(category.getCategoryName());
//
//            return existingCatogory;
//        }
//        else {
//            throw  new ResponseStatusException(HttpStatus.NOT_FOUND,"Category not found");
//        }

    }

}
