package com.ecom.service.impl;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Category;
import com.ecom.repository.CategoryRepository;
import com.ecom.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public Category saveCategory(Category category) {
		return categoryRepository.save(category);
	}

	@Override
	public List<Category> getAllCategory() {
		return categoryRepository.findAll();
	}

	@Override
	public Boolean existCategory(String name) {
		return categoryRepository.existsByName(name);
	}

	@Override
	public Boolean deleteCategory(int id) {

	    Category category = categoryRepository.findById(id).orElse(null);

	    if (category == null) {
	        return false;
	    }

	    // 1️⃣ Delete category from DB
	    categoryRepository.delete(category);

	    // 2️⃣ Delete category image (if not default)
	    if (category.getImageName() != null &&
	        !category.getImageName().equals("default.jpg")) {

	        String projectPath = System.getProperty("user.dir");
	        String imagePath = projectPath
	                + "/src/main/resources/static/img/category_img/"
	                + category.getImageName();

	        File imageFile = new File(imagePath);

	        if (imageFile.exists()) {
	            imageFile.delete();
	        }
	    }

	    return true;
	}


	@Override
	public Category getCategoryById(int id) {
		Category category = categoryRepository.findById(id).orElse(null);
		return category;
	}

	@Override
	public List<Category> getAllActiveCategory() {
		List<Category> categories = categoryRepository.findByIsActiveTrue();
		return categories;
	}

	@Override
	public Page<Category> getAllCategorPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return categoryRepository.findAll(pageable);
	}

}