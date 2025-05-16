package com.example.pos.controller;

import com.example.pos.model.Product;
import com.example.pos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products")
    public String showProductsPage(@RequestParam(required = false) String username, Model model) {
        List<Product> activeProducts = productRepository.findAll().stream()
            .filter(product -> product.getIsDeleted() == null || !product.getIsDeleted())
            .collect(Collectors.toList());

        model.addAttribute("products", activeProducts);
        model.addAttribute("product", new Product());
        if (username != null) model.addAttribute("username", username);
        return "products";
    }

    @PostMapping("/products")
    public String addProduct(@ModelAttribute Product product, Model model) {
        boolean nameExists = productRepository.findAll().stream()
            .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
            .anyMatch(p -> p.getName().equalsIgnoreCase(product.getName().trim()));

        boolean descExists = productRepository.findAll().stream()
            .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
            .anyMatch(p -> p.getDescription().equalsIgnoreCase(product.getDescription().trim()));

        if (nameExists) {
            List<Product> activeProducts = productRepository.findAll().stream()
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .collect(Collectors.toList());
            model.addAttribute("products", activeProducts);
            model.addAttribute("product", product);
            model.addAttribute("errorMessage", "Product name already exists.");
            return "products";
        }

        if (descExists) {
            List<Product> activeProducts = productRepository.findAll().stream()
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .collect(Collectors.toList());
            model.addAttribute("products", activeProducts);
            model.addAttribute("product", product);
            model.addAttribute("errorMessage", "Product description already exists.");
            return "products";
        }

        product.setIsDeleted(false);
        productRepository.save(product);
        return "redirect:/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model, @RequestParam(required = false) String username) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
        model.addAttribute("product", product);

        List<Product> activeProducts = productRepository.findAll().stream()
            .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
            .collect(Collectors.toList());
        model.addAttribute("products", activeProducts);

        if (username != null) model.addAttribute("username", username);
        return "products";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product updatedProduct, Model model) {
        boolean nameExists = productRepository.findAll().stream()
            .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
            .anyMatch(p -> !p.getId().equals(id) &&
                           p.getName().equalsIgnoreCase(updatedProduct.getName().trim()));

        boolean descExists = productRepository.findAll().stream()
            .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
            .anyMatch(p -> !p.getId().equals(id) &&
                           p.getDescription().equalsIgnoreCase(updatedProduct.getDescription().trim()));

        if (nameExists || descExists) {
            List<Product> activeProducts = productRepository.findAll().stream()
                .filter(p -> p.getIsDeleted() == null || !p.getIsDeleted())
                .collect(Collectors.toList());
            model.addAttribute("products", activeProducts);
            model.addAttribute("product", updatedProduct);

            if (nameExists) {
                model.addAttribute("errorMessage", "Another product with the same name already exists.");
            } else {
                model.addAttribute("errorMessage", "Another product with the same description already exists.");
            }

            return "products";
        }

        Product existingProduct = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        productRepository.save(existingProduct);

        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String softDeleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid product ID: " + id));
        product.setIsDeleted(true);
        productRepository.save(product);
        return "redirect:/products";
    }
}
