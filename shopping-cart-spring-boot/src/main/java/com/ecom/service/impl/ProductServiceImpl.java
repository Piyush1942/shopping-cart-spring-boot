package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;
import com.ecom.repository.ProductRepository;
import com.ecom.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Override
	public Product saveProduct(Product product) {
		return productRepository.save(product);
	}

	@Override
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@Override
	public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findAll(pageable);
	}

	@Override
	public Boolean deleteProduct(Integer id) {

	    Product product = productRepository.findById(id).orElse(null);

	    if (product == null) {
	        return false;
	    }

	    // 1️⃣ Delete DB record first
	    productRepository.delete(product);

	    // 2️⃣ Delete image file (after DB delete)
	    if (product.getImage() != null &&
	        !product.getImage().equals("default.jpg")) {

	        String projectPath = System.getProperty("user.dir");
	        String imagePath = projectPath
	                + "/src/main/resources/static/img/product_img/"
	                + product.getImage();

	        File imageFile = new File(imagePath);
	        if (imageFile.exists()) {
	            imageFile.delete();
	        }
	    }

	    return true;
	}


	@Override
	public Product getProductById(Integer id) {
		Product product = productRepository.findById(id).orElse(null);
		return product;
	}

	@Override
	public Product updateProduct(Product product, MultipartFile image) {

	    Product dbProduct = getProductById(product.getId());

	    if (dbProduct == null) {
	        return null;
	    }

	    String oldImage = dbProduct.getImage();
	    String newImageName = oldImage;

	    if (image != null && !image.isEmpty()) {

	        try {
	            String projectPath = System.getProperty("user.dir");
	            String uploadDir = projectPath +
	                    "/src/main/resources/static/img/product_img";

	            File dir = new File(uploadDir);
	            if (!dir.exists()) {
	                dir.mkdirs();
	            }

	            newImageName = System.currentTimeMillis()
	                    + "_" + image.getOriginalFilename();

	            Path newImagePath = Paths.get(uploadDir, newImageName);
	            Files.copy(image.getInputStream(),
	                       newImagePath,
	                       StandardCopyOption.REPLACE_EXISTING);

	            // 🔥 DELETE OLD IMAGE
	            if (oldImage != null && !oldImage.equals("default.jpg")) {
	                File oldFile = new File(uploadDir + "/" + oldImage);
	                if (oldFile.exists()) {
	                    oldFile.delete();
	                }
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    dbProduct.setTitle(product.getTitle());
	    dbProduct.setDescription(product.getDescription());
	    dbProduct.setCategory(product.getCategory());
	    dbProduct.setPrice(product.getPrice());
	    dbProduct.setStock(product.getStock());
	    dbProduct.setImage(newImageName);
	    dbProduct.setIsActive(product.getIsActive());
	    dbProduct.setDiscount(product.getDiscount());

	    Double discountAmount =
	            product.getPrice() * (product.getDiscount() / 100.0);
	    dbProduct.setDiscountPrice(product.getPrice() - discountAmount);

	    return productRepository.save(dbProduct);
	}


	@Override
	public List<Product> getAllActiveProducts(String category) {
		List<Product> products = null;
		if (ObjectUtils.isEmpty(category)) {
			products = productRepository.findByIsActiveTrue();
		} else {
			products = productRepository.findByCategory(category);
		}

		return products;
	}

	@Override
	public List<Product> searchProduct(String ch) {
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
	}

	@Override
	public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
	}

	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize, String category) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);
		Page<Product> pageProduct = null;

		if (ObjectUtils.isEmpty(category)) {
			pageProduct = productRepository.findByIsActiveTrue(pageable);
		} else {
			pageProduct = productRepository.findByCategory(pageable, category);
		}
		return pageProduct;
	}

	@Override
	public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {

		Page<Product> pageProduct = null;
		Pageable pageable = PageRequest.of(pageNo, pageSize);

		pageProduct = productRepository.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,
				ch, pageable);

//		if (ObjectUtils.isEmpty(category)) {
//			pageProduct = productRepository.findByIsActiveTrue(pageable);
//		} else {
//			pageProduct = productRepository.findByCategory(pageable, category);
//		}
		return pageProduct;
	}

}