package com.ecom.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* ================= COMMON ================= */

    @ModelAttribute
    public void addCommonData(Principal p, Model m) {
        if (p != null) {
            UserDtls user = userService.getUserByEmail(p.getName());
            m.addAttribute("user", user);
            m.addAttribute("countCart", cartService.getCountCart(user.getId()));
        }
        m.addAttribute("categorys", categoryService.getAllActiveCategory());
    }

    @GetMapping("/")
    public String dashboard() {
        return "admin/index";
    }

    /* ================= CATEGORY ================= */

    @GetMapping("/category")
    public String categoryPage(Model m,
                               @RequestParam(defaultValue = "0") int pageNo,
                               @RequestParam(defaultValue = "10") int pageSize) {

        Page<Category> page = categoryService.getAllCategorPagination(pageNo, pageSize);

        m.addAttribute("categorys", page.getContent());
        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam("file") MultipartFile file,
                               HttpSession session) {

        if (categoryService.existCategory(category.getName())) {
            session.setAttribute("errorMsg", "Category already exists");
            return "redirect:/admin/category";
        }

        try {
            String imageName = "default.jpg";

            if (!file.isEmpty()) {
                String uploadDir = System.getProperty("user.dir")
                        + "/src/main/resources/static/img/category_img";

                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                imageName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir, imageName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            category.setImageName(imageName);
            categoryService.saveCategory(category);
            session.setAttribute("succMsg", "Category saved successfully");

        } catch (Exception e) {
            session.setAttribute("errorMsg", "Image upload failed");
        }

        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession session) {

        if (categoryService.deleteCategory(id)) {
            session.setAttribute("succMsg", "Category deleted");
        } else {
            session.setAttribute("errorMsg", "Delete failed");
        }

        return "redirect:/admin/category";
    }

    /* ================= PRODUCT ================= */

    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model m) {
        m.addAttribute("categories", categoryService.getAllCategory());
        return "admin/add_product";
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product,
                               @RequestParam("file") MultipartFile file,
                               HttpSession session) {

        try {
            String imageName = "default.jpg";

            if (!file.isEmpty()) {
                String uploadDir = System.getProperty("user.dir")
                        + "/src/main/resources/static/img/product_img";

                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                imageName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir, imageName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            product.setImage(imageName);
            product.setDiscount(0);
            product.setDiscountPrice(product.getPrice());
            product.setIsActive(true);

            productService.saveProduct(product);
            session.setAttribute("succMsg", "Product saved successfully");

        } catch (Exception e) {
            session.setAttribute("errorMsg", "Product save failed");
        }

        return "redirect:/admin/loadAddProduct";
    }

    @GetMapping("/products")
    public String viewProducts(Model m,
                               @RequestParam(defaultValue = "0") int pageNo,
                               @RequestParam(defaultValue = "10") int pageSize) {

        Page<Product> page = productService.getAllProductsPagination(pageNo, pageSize);

        m.addAttribute("products", page.getContent());
        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        return "admin/products";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id, HttpSession session) {

        if (productService.deleteProduct(id)) {
            session.setAttribute("succMsg", "Product deleted");
        } else {
            session.setAttribute("errorMsg", "Delete failed");
        }

        return "redirect:/admin/products";
    }
    
    @GetMapping("/users")
    public String loadUsers(Model m,
            @RequestParam(defaultValue = "USER") String type) {

        System.out.println("Fetching users for role = " + type);

        List<UserDtls> users = userService.getUsers(type);

        m.addAttribute("users", users);
        m.addAttribute("userType", type);

        return "admin/users";
    }

  
    @GetMapping("/updateSts")
    public String updateStatus(@RequestParam Boolean status,
                               @RequestParam Integer id,
                               @RequestParam String type,
                               HttpSession session) {

        Boolean updated = userService.updateAccountStatus(id, status);

        if (updated) {
            session.setAttribute("succMsg", "Status updated successfully");
        } else {
            session.setAttribute("errorMsg", "Status update failed");
        }

        return "redirect:/admin/users?type=" + type;
    }

    
    /* ================= ADD ADMIN PAGE ================= */

    @GetMapping("/add-admin")
    public String loadAddAdmin() {
        return "admin/add_admin";
    }

    /* ================= SAVE ADMIN ================= */

    @PostMapping("/save-admin")
    public String saveAdmin(@ModelAttribute UserDtls user,
                            @RequestParam(required = false) MultipartFile img,
                            HttpSession session) {

        if (userService.existsEmail(user.getEmail())) {
            session.setAttribute("errorMsg", "Email already exists");
            return "redirect:/admin/add-admin";
        }

        try {
            // default image
            user.setProfileImage("default.jpg");

            if (img != null && !img.isEmpty()) {
                String uploadDir = System.getProperty("user.dir")
                        + "/src/main/resources/static/img/profile_img";

                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = UUID.randomUUID() + "_" + img.getOriginalFilename();
                Path path = Paths.get(uploadDir, fileName);
                Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                user.setProfileImage(fileName);
            }

            userService.saveAdmin(user);
            session.setAttribute("succMsg", "Admin added successfully");

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMsg", "Admin not saved");
        }

        return "redirect:/admin/add-admin";
    }
}
