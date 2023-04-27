package com.ssafy.nanumi.api.service;

import com.ssafy.nanumi.api.request.ProductInsertDTO;
import com.ssafy.nanumi.api.response.MatchSuccessDto;
import com.ssafy.nanumi.api.response.ProductAllDTO;
import com.ssafy.nanumi.api.response.ProductDetailDTO;
import com.ssafy.nanumi.config.response.exception.CustomException;
import com.ssafy.nanumi.config.response.exception.CustomExceptionStatus;
import com.ssafy.nanumi.db.entity.*;
import com.ssafy.nanumi.db.repository.CategoryRepository;
import com.ssafy.nanumi.db.repository.MatchRepository;
import com.ssafy.nanumi.db.repository.ProductImageRepository;
import com.ssafy.nanumi.db.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final MatchRepository matchRepository;

    public Page<ProductAllDTO> findProductAll(User user, PageRequest pageRequest) {
        Long addressId = user.getAddress().getId();
        return productRepository.findAllProduct(addressId, pageRequest);
    }

    public ProductDetailDTO findByProductId(Long id) {
        return productRepository.findById(id)
                .map(ProductDetailDTO::new)
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_PRODUCT));
    }

    public Page<ProductAllDTO> findCateProductAll(Long categoryId, User user, Pageable pageRequest) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(CustomExceptionStatus.NOT_FOUND_CATEGORY));
        Long addressId = user.getAddress().getId();
        return productRepository.findAllCategoryProuduct(addressId,categoryId, pageRequest);
}

    public void createProduct(ProductInsertDTO request, User user){
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_CATEGORY));
        Address address = user.getAddress();
        Product product = Product.builder()
                .name(request.getName())
                .content(request.getContent())
                .isClosed(false)
                .isDeleted(false)
                .user(user)
                .category(category)
                .address(address)
                .build();
        Product createProduct = productRepository.save(product);
        List<String> images = request.getPostImage();
        for (String image : images) {
            ProductImage productImage = ProductImage.builder()
                    .imageUrl(image)
                    .product(createProduct)
                    .build();
            productImageRepository.save(productImage);
        }
    }
    public void updateProduct(ProductInsertDTO request, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_PRODUCT));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_CATEGORY));
        product.setName(request.getName());
        product.setContent(request.getContent());
        product.setCategory(category);
    }
    public void deleteProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_PRODUCT));
        product.delete();
    }
    public MatchSuccessDto applicationProduct(Long id, User user) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new CustomException(CustomExceptionStatus.NOT_FOUND_PRODUCT));
        if ((long) product.getMatches().size() < 3){
            Match match = Match.builder()
                    .isMatching(false)
                    .product(product)
                    .user(user)
                    .build();
            Match newMatch = matchRepository.save(match);
            return MatchSuccessDto.builder()
                    .result(true)
                    .resultMessage("신청 되었습니다.")
                    .matchId(newMatch.getId())
                    .build();
        }
        else {
            product.setClosed(true);
            return MatchSuccessDto.builder()
                    .result(false)
                    .resultMessage("인원이 다 찼습니다.")
                    .matchId(null)
                    .build();
        }
    }
}