package com.ssafy.nanumi.db.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name="name", columnDefinition="VARCHAR(20)", nullable = false)
    private String name;

    @OneToMany(mappedBy = "category")
    private List<Product>  products = new ArrayList<>();

    @Builder
    public Category (String name){
        this.name = name;
    }
}
