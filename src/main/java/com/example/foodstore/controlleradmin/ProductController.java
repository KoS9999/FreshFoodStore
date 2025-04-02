package com.example.foodstore.controlleradmin;

import com.example.foodstore.entity.Category;
import com.example.foodstore.entity.Product;
import com.example.foodstore.entity.ProductImage;
import com.example.foodstore.service.CategoryService;
import com.example.foodstore.service.ProductImageService;
import com.example.foodstore.service.ProductService;
import com.example.foodstore.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductImageService productImageService;

    @GetMapping
    public String viewProductPage(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/product";
    }
    @GetMapping("/add")
    public String addProductPage(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/add-product";
    }
    @GetMapping("/addImages/{productId}")
    public String addProductImagesPage(@PathVariable("productId") Long productId, Model model) {
        Product product = productService.findById(productId);
        if (product == null) {
            return "redirect:/admin/products?error=product_not_found";
        }
        List<ProductImage> additionalImages = productImageService.findByProduct(product);
        model.addAttribute("product", product);
        model.addAttribute("additionalImages", additionalImages);
        return "admin/add-product-images";
    }


    @PostMapping("/saveAdditionalImages/{productId}")
    public String addProductImages(@PathVariable("productId") Long productId,
                                   @RequestParam("additionalImageFiles") MultipartFile[] additionalImageFiles) throws IOException {
        Product product = productService.findById(productId);
        if (product == null) {
            return "redirect:/admin/products?error=product_not_found";
        }
        File additionalUploadPath = new File(uploadDir + "/additional/");
        if (!additionalUploadPath.exists()) {
            additionalUploadPath.mkdirs();
        }
        for (MultipartFile file : additionalImageFiles) {
            if (!file.isEmpty()) {
                String additionalFileName = file.getOriginalFilename();
                File destinationFile = new File(additionalUploadPath, additionalFileName);
                Thumbnails.of(file.getInputStream())
                        .size(400, 400)
                        .toFile(destinationFile);
                ProductImage additionalImage = new ProductImage();
                additionalImage.setImageUrl(additionalFileName);
                additionalImage.setProduct(product);
                productImageService.save(additionalImage);
                System.out.println("Saved additional image: " + additionalFileName);
            } else {
                System.out.println("File is empty: " + file.getOriginalFilename());
            }
        }
        return "redirect:/admin/products";
    }



    @Value("${image.upload.dir}")
    private String uploadDir;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute("product") Product product,
                              @RequestParam("categoryId") Long categoryId,
                              @RequestParam("mainImageFile") MultipartFile mainImageFile,
                              @RequestParam(value = "seasonMonths", required = false) List<Integer> seasonMonths) throws IOException {
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            return "redirect:/admin/products?error=invalid_category";
        }
        product.setCategory(category);

        // Gán mùa nếu có chọn
        product.setSeasonMonths(seasonMonths != null ? seasonMonths : List.of());

        if (!mainImageFile.isEmpty()) {
            String mainFileName = mainImageFile.getOriginalFilename();
            File mainUploadPath = new File(uploadDir + "/main/");
            mainUploadPath.mkdirs();
            File mainImageDestinationFile = new File(mainUploadPath, mainFileName);
            Thumbnails.of(mainImageFile.getInputStream())
                    .size(800, 800)
                    .toFile(mainImageDestinationFile);
            product.setProductImage(mainFileName);
        }

        Product savedProduct = productService.save(product);
        return "redirect:/admin/products/addImages/" + savedProduct.getProductId();
    }


    @GetMapping("/edit/{id}")
    public String editProductPage(@PathVariable("id") Long id, Model model) {
        Product product = productService.findById(id);
        List<Category> categories = categoryService.findAll();

        if (product == null) {
            return "redirect:/admin/products?error=product_not_found";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "admin/edit-product";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") Long id,
                                @ModelAttribute("product") Product product,
                                @RequestParam("categoryId") Long categoryId,
                                @RequestParam(value = "mainImageFile", required = false) MultipartFile mainImageFile,
                                @RequestParam(value = "additionalImageFiles", required = false) MultipartFile[] additionalImageFiles,
                                @RequestParam(value = "seasonMonths", required = false) List<Integer> seasonMonths) throws IOException {
        Product existingProduct = productService.findById(id);
        if (existingProduct == null) {
            return "redirect:/admin/products?error=not_found";
        }

        existingProduct.setProductName(product.getProductName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDiscount(product.getDiscount());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setEnteredDate(product.getEnteredDate());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setStatus(product.getStatus());
        existingProduct.setSeasonMonths(seasonMonths != null ? seasonMonths : List.of());

        Category category = categoryService.findById(categoryId);
        if (category != null) {
            existingProduct.setCategory(category);
        } else {
            return "redirect:/admin/products?error=invalid_category";
        }

        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String mainImagePath = saveMainImage(mainImageFile);
            existingProduct.setProductImage(mainImagePath);
        }

        if (additionalImageFiles != null) {
            productService.deleteProductImages(existingProduct);
            for (MultipartFile file : additionalImageFiles) {
                if (!file.isEmpty()) {
                    String additionalImagePath = saveAdditionalImage(file);
                    productService.addProductImage(existingProduct, additionalImagePath);
                }
            }
        }

        productService.save(existingProduct);
        return "redirect:/admin/products";
    }

    // Phương thức lưu ảnh chính
    private String saveMainImage(MultipartFile file) throws IOException {
        String mainUploadDir = new File("uploads/main").getAbsolutePath();
        String fileName = file.getOriginalFilename();
        FileUploadUtil.saveFile(mainUploadDir, fileName, file);
        return fileName;
    }
    // Phương thức lưu ảnh phụ
    private String saveAdditionalImage(MultipartFile file) throws IOException {
        String additionalUploadDir = new File("uploads/additional").getAbsolutePath();
        String fileName = file.getOriginalFilename();
        FileUploadUtil.saveFile(additionalUploadDir, fileName, file);
        return fileName;
    }
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        productService.delete(id);
        return "redirect:/admin/products";
    }
    @DeleteMapping("/delete-image/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long imageId) {
        try {
            productImageService.deleteImageById(imageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }









}
