package com.ecommerce.project.services;

import com.ecommerce.project.customExceptionHandler.ApiException;
import com.ecommerce.project.customExceptionHandler.ResourseNorFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService{
    @Autowired
  private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartService cartService;


    @Autowired
   private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${image.path}")
    String path;


    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourseNorFoundException("Category","categoryId",categoryId));
        if (category==null)
            throw new ApiException("Category with the name '"+ category.getCategoryName() +"' is Not exists..!");

        boolean isProductNotPresent=true;

        List<Product> products=category.getProducts();
        for (int i=0;i<products.size();i++){
            if(products.get(i).getProductName().equals(productDTO.getProductName())){
            isProductNotPresent=false;
            break;
            }
        }
        if(isProductNotPresent) {
            Product product = modelMapper.map(productDTO, Product.class);
            product.setCategory(category);
            double specialPrice = product.getPrice() - (product.getDiscount() * 0.01) * product.getPrice();
            product.setImage(product.getImage());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);

            return modelMapper.map(savedProduct, ProductDTO.class);
        }
        else {
            throw new ApiException("Product already Present");
        }
    }

//    @Override
//    public ProductResponse getAllCategories() {
//        List<Product> products=productRepository.findAll();
//        List<ProductDTO>productDTOS=products.stream()
//                .map(product -> modelMapper.map(product,ProductDTO.class))
//                .toList();
//        if (products.isEmpty())
//            throw new ApiException("there are no Products to fetch");
//        ProductResponse productResponse=new ProductResponse();
//        productResponse.setContent(productDTOS);
//        return productResponse;
//    }
@Override
public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
    Sort sortAndOrderBy=sortOrder.equalsIgnoreCase("asc")
            ?Sort.by(sortBy).ascending()
            :Sort.by(sortBy).descending();

    Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortAndOrderBy);
    Page<Product> productPage=productRepository.findAll(pageDetails);
    List<Product>savedProducts=productPage.getContent();
//    if (savedProducts.isEmpty())
//        throw new ApiException("there are no Products to fetch");
    List<ProductDTO> productDTOS=savedProducts.stream()
            .map(product -> modelMapper.map(product,ProductDTO.class))
            .toList();
    ProductResponse productResponse=new ProductResponse();

    productResponse.setContent(productDTOS);
    productResponse.setPageNumber(productPage.getNumber());
    productResponse.setPageSize(productPage.getSize());
    productResponse.setTotalPages(productPage.getTotalPages());
    productResponse.setTotalElements(productPage.getTotalElements());
    productResponse.setLastPage(productPage.isLast());
    return productResponse;



}

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourseNorFoundException("Category","categoryId",categoryId));

        Sort sortAndOrderBy=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortAndOrderBy);
        Page<Product> productPage=productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);

        List<Product>savedProducts=productPage.getContent();
        if (savedProducts.isEmpty())
            throw new ApiException("there are no Products to fetch with this category ; "+category);

        List<ProductDTO>productDTOS=savedProducts.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();
        ProductResponse productResponse=new ProductResponse();

        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortAndOrderBy=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortAndOrderBy);
        Page<Product> productPage=productRepository.findByProductNameLikeIgnoreCase("%"+keyword+"%",pageDetails);

//        List<Product>products=productRepository.findByProductNameLikeIgnoreCase("%"+keyword+"%", pageDetails);//% is to implement Like method in pattern matching
        List<Product>savedProducts=productPage.getContent();
        if (savedProducts.isEmpty())
            throw new ApiException("there are no Products to fetch with keyWord; "+keyword);
        List<ProductDTO>productDTOS=savedProducts.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();
        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        //get existing product
        Product savedProduct=productRepository.findById(productId)
                .orElseThrow(()->new ResourseNorFoundException("Product","productId",productId));
//convert productDTO into Product
        Product product=modelMapper.map(productDTO,Product.class);
        //update product info with user shared
        savedProduct.setProductName(product.getProductName());
        savedProduct.setDescription(product.getDescription());
        savedProduct.setQuantity(product.getQuantity());
        savedProduct.setDiscount(product.getDiscount());
        savedProduct.setPrice(product.getPrice());
        double specialPrice=product.getPrice()-(product.getDiscount()*0.01)*product.getPrice();
        savedProduct.setSpecialPrice(specialPrice);
        Product updatedProduct=productRepository.save(savedProduct);

        List<Cart> carts=cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOS=carts.stream().map(cart -> {
           CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);

           List<ProductDTO> products=cart.getCartItems().stream().
                   map(p->modelMapper.map(p.getProduct(),ProductDTO.class)).toList();

           cartDTO.setProducts(products);

           return cartDTO;
        }).collect(Collectors.toList());

        cartDTOS.forEach(cart->cartService.updateProductsInCarts(cart.getCartId(),productId));
        return modelMapper.map(savedProduct,ProductDTO.class);


//        return modelMapper.map(updatedProduct,ProductDTO.class);


    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        //Get Product From Db
        Product productFromDb=productRepository.findById(productId)
                .orElseThrow(()->new ResourseNorFoundException("Product","productId",productId));
        //upload image to server
        //set the file name of uploaded image
//        String path="images/";
        String fileName=fileService.uploadImage(path,image);

        //updating the new file name to the product
        productFromDb.setImage(fileName);

        //save updated product
        Product updatedProduct=productRepository.save(productFromDb);


        return modelMapper.map(updatedProduct,ProductDTO.class);
    }



    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product savedProduct=productRepository.findById(productId)
                .orElseThrow(()->new ResourseNorFoundException("Product","productId",productId));

        List<Cart> carts=cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(),productId));

//        ProductDTO productDTO=modelMapper.map(savedProduct,ProductDTO.class);
        productRepository.delete(savedProduct);
        return modelMapper.map(savedProduct,ProductDTO.class);

    }

}
