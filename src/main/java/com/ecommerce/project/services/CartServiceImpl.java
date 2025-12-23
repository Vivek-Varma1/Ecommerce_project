package com.ecommerce.project.services;

import com.ecommerce.project.customExceptionHandler.ApiException;
import com.ecommerce.project.customExceptionHandler.ResourseNorFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{
    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private CartItemRepository cartItemRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;
    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
       // Find Existing cart or create one
       Cart cart= createCart();

       Product product=productRepo.findById(productId)
               .orElseThrow(()->new ResourseNorFoundException("Product","productId",productId));

        CartItem cartItem=cartItemRepo.findCartItemByCartIdAndProductId(cart.getCartId(),productId);
        if (cartItem!=null)
            throw new ApiException("Product "+product.getProductName()+" already Exists in the Cart");
        if (product.getQuantity()==0)
            throw new ApiException(product.getProductName()+" is not available");
        if (product.getQuantity()<quantity)
            throw new ApiException("Please , make an order of the "+product.getProductName()+
                    " less than or equals to the product quantity "+ product.getQuantity()+" .");
        CartItem newCartItem=new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getPrice());

        cartItemRepo.save(newCartItem);

        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice()+(product.getSpecialPrice()*quantity));

        cartRepo.save(cart);

        CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);

        List<CartItem>cartItems=cart.getCartItems();

        Stream<ProductDTO>productStream=cartItems.stream().map(item->{
                ProductDTO map=modelMapper.map(item.getProduct(),ProductDTO.class);
        map.setQuantity(item.getQuantity());
        return map;
                });

        cartDTO.setProducts(productStream.toList());
        return cartDTO;
        //Retrive product details
        //perform valodations (check if product already in the cart is the product that is trying to add has suffice=ient stock that user want to add quantity  if user ADD QUANTITY 10 BUT THE STOCK IS ONLY 7
        //Create Cart Item
        //Save Cart item
        //retrun updated cart
    }

    @Override
    public List<CartDTO> getAllCarts() {
       List<Cart> carts=  cartRepo.findAll();
//       if (carts==null)
//           throw new ApiException("Cart is empty");
       if (carts.isEmpty())
           throw new ApiException("Your Cart is Empty....!");

        List<CartDTO> cartDTOS=carts.stream().map(cart -> {
            CartDTO cartDTO =modelMapper.map(cart,CartDTO.class);

//            List<ProductDTO>products=cart.getCartItems().stream()
//                    .map(p->modelMapper.map(p.getProduct(),ProductDTO.class)).toList();
            List<ProductDTO> products=cart.getCartItems().stream().map(item->{
                ProductDTO productDTO=modelMapper.map(item.getProduct(),ProductDTO.class);
                productDTO.setQuantity(item.getQuantity());
                return productDTO;
            }).toList();

            cartDTO.setProducts(products);
            return cartDTO;
        }).toList();

       return cartDTOS;

    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart =cartRepo.findCartByEmailAndCartId(emailId,cartId);

        if (cart==null)
            throw new ResourseNorFoundException("Cart","cartId",cartId);

        CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);
        cart.getCartItems().forEach(c->
                c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO>products=cart.getCartItems().stream()
                .map(p->modelMapper.map(p.getProduct(),ProductDTO.class)).toList();
        cartDTO.setProducts(products);
        return cartDTO;

    }

    @Transactional
    @Override
    public CartDTO updateProductQuamtityInCart(Long productId, Integer quantity) {
       String emailId=authUtil.loggedInEmail();
       Cart userCart=cartRepo.findCartByEmail(emailId);
       Long cartId=userCart.getCartId();

       Cart cart=cartRepo.findById(cartId)
               .orElseThrow(()->new ResourseNorFoundException("Cart","CartId",cartId));

       Product product=productRepo.findById(productId)
               .orElseThrow(()->new ResourseNorFoundException("Product","ProductId",productId));

       if (product.getQuantity()==0)
           throw new ApiException(product.getProductName()+" is not available");

        CartItem cartItem=cartItemRepo.findCartItemByCartIdAndProductId(cartId,productId);
        if(cartItem==null)
            throw new ApiException("Product "+product.getProductName()+" not Available in the cart");

       if(quantity>0 && product.getQuantity()<(cartItem.getQuantity() + (quantity)))
           throw new ApiException("Insufficient stock for '" + product.getProductName() +
                   "'. Please reduce the quantity to " + product.getQuantity() + " or fewer.");

       int newQuantity=cartItem.getQuantity() + quantity;
       if (newQuantity<0){
           throw new ApiException("The resulting quantity cannot be negative...!!");
       }
       if (newQuantity==0){
           deleteProductFromCart(cartId,productId);
       }
       else {
           cartItem.setProductPrice(product.getSpecialPrice());
           cartItem.setQuantity(cartItem.getQuantity() + quantity);
           cartItem.setDiscount(product.getDiscount());
           cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
//        cart.setTotalPrice(
//                cart.getCartItems().stream()
//                        .mapToDouble(item -> item.getProductPrice() * item.getQuantity())
//                        .sum()
//        );


           cartRepo.save(cart);
       }
       CartItem updatedItem=cartItemRepo.save(cartItem);
       if (updatedItem.getQuantity()==0) {
           cartItemRepo.deleteById(updatedItem.getCartItemId());
       }

       CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);
       List<CartItem> cartItems=cart.getCartItems();

       Stream<ProductDTO> productDTOStream=cartItems.stream().map(item->{
           ProductDTO productDTO=modelMapper.map(item.getProduct(),ProductDTO.class);
           productDTO.setQuantity(item.getQuantity());
           return productDTO;
       });
       cartDTO.setProducts(productDTOStream.toList());
       return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart=cartRepo.findById(cartId)
                .orElseThrow(()->new ResourseNorFoundException("Cart","CartId",cartId));

        CartItem cartItem=cartItemRepo.findCartItemByCartIdAndProductId(cartId,productId);
        if (cartItem==null)
            throw new ResourseNorFoundException("Product","ProductId",productId);

//        cart.setTotalPrice(cart.getTotalPrice()-(cartItem.getProductPrice() * cartItem.getQuantity()));
        double newTotal = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());
        cart.setTotalPrice(Math.max(0, newTotal));


        // Product product=cartItem.getProduct();
      //  product.setQuantity(product.getQuantity() + cartItem.getQuantity());

        cartItemRepo.deleteCartItemByCartIdAndProductId(cartId,productId);

        return "Product "+ cartItem.getProduct().getProductName()+" removed from the cart !!";

    }

    @Override
    public void updateProductsInCarts(Long cartId, Long productId) {
        Cart cart=cartRepo.findById(cartId)
                .orElseThrow(()-> new ResourseNorFoundException("Cart","CartId",cartId));

        Product product=productRepo.findById(productId)
                .orElseThrow(()-> new ResourseNorFoundException("Product","ProductId",productId));

        CartItem cartItem=cartItemRepo.findCartItemByCartIdAndProductId(cartId,productId);

        if (cartItem == null){
            throw new ApiException("Product "+product.getProductName()+" not found in the cart");
            }
        double cartPrice=cart.getTotalPrice() + (cartItem.getProductPrice() *cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem=cartItemRepo.save(cartItem);
    }

    private Cart createCart() {


        Cart userCart=cartRepo.findCartByEmail(authUtil.loggedInEmail());
        if(userCart !=null)
            return userCart;

        Cart cart=new Cart();
        cart.setUser(authUtil.loggedInUser());
        cart.setTotalPrice(0.00);

        return cartRepo.save(cart);
    }

}
