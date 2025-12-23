package com.ecommerce.project.payload;

import com.ecommerce.project.model.Cart;

public class CartItemDTO {
    private Long cartItemId;
    private CartDTO cart;
    private ProductDTO productDTO;
    private Integer quantity;
    private Double discount;
    private Double productPrice;

}
