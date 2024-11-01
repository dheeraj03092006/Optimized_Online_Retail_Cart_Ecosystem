package Optimized_Online_Retail_Cart_Ecosystem;

class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int quantity;

    public Product(String name, String description, double price, int quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    // Getters and setters...
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', description='" + description + "', price=" + price + ", quantity=" + quantity + '}';
    }

    String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    int getQuantity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    double getPrice() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}