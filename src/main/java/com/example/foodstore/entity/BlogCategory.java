package com.example.foodstore.entity;

import lombok.Getter;

@Getter
public enum BlogCategory {
    RECIPES_VEGETARIAN("Công thức món chay"),
    RECIPES_SAVORY("Công thức món mặn"),
    PRODUCT_REVIEWS("Đánh giá sản phẩm"),
    NUTRITION_AND_HEALTH("Kiến thức dinh dưỡng"),
    LIFESTYLE("Phong cách sống"),
    PERSONAL_STORIES("Câu chuyện cá nhân"),
    NEWS_AND_TRENDS("Tin tức & Xu hướng"),
    SPECIAL_TOPICS("Chuyên đề cho đối tượng");

    private final String displayName;

    BlogCategory(String displayName) {
        this.displayName = displayName;
    }

}