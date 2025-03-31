package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Blog;
import com.example.foodstore.repository.BlogRepository;
import com.example.foodstore.service.BlogService;
import com.example.foodstore.service.FirebaseStorageService;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BlogServiceImpl implements BlogService {
    private final BlogRepository blogRepository;
    private final FirebaseStorageService firebaseService;

    public BlogServiceImpl(BlogRepository blogRepository, FirebaseStorageService firebaseService) {
        this.blogRepository = blogRepository;
        this.firebaseService = firebaseService;
    }

    @Override
    public Blog createBlog(Blog blog, MultipartFile imageFile) {
        blog.setCreatedAt(LocalDateTime.now());
        blog.setUpdatedAt(LocalDateTime.now());
        blog.setHtmlContent(convertMarkdownToHtml(blog.getMarkdownContent()));

        // 👉 Upload ảnh nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = firebaseService.uploadFileBlogs(imageFile);
            blog.setImageUrl(imageUrl);
        }

        return blogRepository.save(blog);
    }

    @Override
    public Blog updateBlog(Long id, Blog blog, MultipartFile imageFile) {
        Optional<Blog> existingBlogOpt = blogRepository.findById(id);
        if (existingBlogOpt.isPresent()) {
            Blog existingBlog = existingBlogOpt.get();

            existingBlog.setTitle(blog.getTitle());
            existingBlog.setSlug(blog.getSlug());
            existingBlog.setAuthor(blog.getAuthor());
            existingBlog.setMarkdownContent(blog.getMarkdownContent());
            existingBlog.setHtmlContent(convertMarkdownToHtml(blog.getMarkdownContent()));
            existingBlog.setStatus(blog.getStatus());
            existingBlog.setUpdatedAt(LocalDateTime.now());

            if (blog.getRelatedProducts() != null) {
                existingBlog.setRelatedProducts(blog.getRelatedProducts());
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = firebaseService.uploadFileBlogs(imageFile);
                existingBlog.setImageUrl(imageUrl);
            }

            return blogRepository.save(existingBlog);
        }
        return null;
    }


    @Override
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }

    @Override
    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findById(id);
    }

    @Override
    public Optional<Blog> getBlogBySlug(String slug) {
        return blogRepository.findBySlug(slug);
    }

    @Override
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
