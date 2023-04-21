package com.ssafy.nanumi.api.response;

import com.ssafy.nanumi.db.entity.Product;

import lombok.Getter;


@Getter
public class ProductAllResponse {
    private final Long id;
    private final String name;
    private final String userProfileUrl;
    private final String productImageUrl;
    public ProductAllResponse(Product product){
        id = product.getId();
        name = product.getName();
        userProfileUrl = product.getUser().getProfileUrl();
        productImageUrl = product.getProductImages().get(0).getImageUrl();
    }
}
