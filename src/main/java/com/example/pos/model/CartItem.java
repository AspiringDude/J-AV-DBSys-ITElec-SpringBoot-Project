package com.example.pos.model;

public class CartItem {
    private Product product;
    private int quantity;
    private double totalPrice;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public void calculateTotalPrice() {
        this.totalPrice = product.getPrice() * quantity;
    }

    // Getters and setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
}

