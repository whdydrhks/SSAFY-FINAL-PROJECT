package com.ssafy.nanumi.db.repository;

import com.ssafy.nanumi.api.response.ProductAllDTO;
import com.ssafy.nanumi.api.response.ReviewReadDTO;
import com.ssafy.nanumi.common.provider.Provider;
import com.ssafy.nanumi.db.entity.LoginProvider;
import com.ssafy.nanumi.db.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.parameters.P;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmailAndLoginProvider(String email, LoginProvider provider);
    Optional<User> findByEmail(String email);

    @Query(value = "select p " +
            "from Review p " +
            "where p.receiver.id = :userId")
    Page<ReviewReadDTO> getAllReview(@Param("userId") long userId, Pageable pageable) ;

    @Query(value = "select p " +
            "from Product p " +
            "where p.user.id = :userId " +
            "and p.isDeleted = false " +
            "and p.isMatched = true ")
    Page<ProductAllDTO> getAllReceiveProduct(@Param("userId") long userId, Pageable pageable);

    @Query(value = "select p " +
            "from Product p " +
            "where p.user.id = :userId " +
            "and p.isDeleted = false "+
            "and p.isMatched = false ")
    Page<ProductAllDTO> getAllMatchProduct(@Param("userId") long userId, Pageable pageable);

    @Query(value = "select p " +
            "from Product p " +
            "where p.user.id = :userId " +
            "and p.Matches.size > 0 " +
            "and p.isDeleted = false " +
            "and p.isMatched = false "
    )
    Page<ProductAllDTO> getAllMatchingProduct(@Param("userId") long userId, Pageable pageable);

    @Query(value = "select p " +
            "from Product p " +
            "left join p.Matches m " +
            "where m.user.id = :userId " +
            "and m.isMatching = true "
    )
    Page<ProductAllDTO> getAllGivenProduct(@Param("userId") long userId, Pageable pageable);
}
