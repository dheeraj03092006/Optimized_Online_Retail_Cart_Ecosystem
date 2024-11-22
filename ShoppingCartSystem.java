import java.sql.*;
import java.util.*;


class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/shopping_cart1";
    private static final String USER = "root";
    private static final String PASSWORD = "Santosh@2006";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Error occurred while establishing the database connection: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database connection failed!");
        }
    }
}

// Customer class
class Customer {
    private int id;
    private String name;
    private String email;
    private String address;

    public Customer(String name, String email, String address) {
        this.name = name;
        this.email = email;
        this.address = address;
    }

    // Getters and setters...
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", name='" + name + "', email='" + email + "', address='" + address + "'}";
    }
}

// CustomerDAO class
class CustomerDAO {
    private Connection connection;

    public CustomerDAO(Connection connection) {
        this.connection = connection;
    }

    public void addCustomer(Customer customer) {
        String query = "INSERT INTO customers (name, email, address) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getAddress());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                customer.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Failed to add customer.");
            e.printStackTrace();
        }
    }

    public boolean deleteCustomer(int customerId) {
        String query = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // Returns true if the customer was deleted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

// Order class
class Order {
    private int id;
    private int customerId;
    private double total;

    public Order(int customerId, double total) {
        this.customerId = customerId;
        this.total = total;
    }

    // Getters and setters...
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public double getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", customerId=" + customerId + ", total=" + total + '}';
    }
}

// OrderDAO class
class OrderDAO {
    private Connection connection;

    public OrderDAO() {
        connection = DatabaseConnection.getConnection();
    }

    public void addOrderItem(int customerId, int productId, int quantity, double price) {
        String query = "INSERT INTO order_items (customer_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, price);
            stmt.executeUpdate();
            System.out.println("Order item added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getOrderItems(int orderId) {
        String query = "SELECT oi.id, p.name, oi.quantity, oi.price " +
                       "FROM order_items oi " +
                       "JOIN products p ON oi.product_id = p.id " +
                       "WHERE oi.id = ?";
        List<String> orderItems = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String itemDetails = "Item ID: " + rs.getInt("id") +
                                     ", Product: " + rs.getString("name") +
                                     ", Quantity: " + rs.getInt("quantity") +
                                     ", Price: $" + rs.getDouble("price");
                orderItems.add(itemDetails);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderItems;
    }

    public void processPayment(int orderId, String paymentMethod) {
        String query = "INSERT INTO payments (order_id, payment_method, payment_status, transaction_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            stmt.setString(2, paymentMethod);
            stmt.setString(3, "Completed");
            if (query.contains("transaction_date")) {
                stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // If payment_date is used
            }
            stmt.executeUpdate();
            System.out.println("Payment processed successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addOrder(Order order) {
        String query = "INSERT INTO orders (customer_id, total) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getCustomerId());
            stmt.setDouble(2, order.getTotal());
            stmt.executeUpdate();
    
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                order.setId(generatedKeys.getInt(1));
            }
            System.out.println("Order created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



// Product class
class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private int categoryId;

    public Product(int id, String name, String description, double price, int quantity, int categoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.categoryId = categoryId;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter methods
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCategoryId() {
        return categoryId;
    }

    @Override
    public String toString() {
        return "Product ID: " + id +
               ", Name: " + name +
               ", Description: " + description +
               ", Price: $" + price +
               ", Quantity: " + quantity +
               ", Category ID: " + categoryId;
    }
}



// ProductDAO class

class ProductDAO {
    private Connection connection;

    public ProductDAO() {
        connection = DatabaseConnection.getConnection();
    }

    public List<Product> getAllProducts() {
        String query = "SELECT * FROM products";
        List<Product> products = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getInt("category_id")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void addReview(int productId, int customerId, int rating, String reviewText) {
        String query = "INSERT INTO reviews (product_id, customer_id, rating, review_text) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, customerId);
            stmt.setInt(3, rating);
            stmt.setString(4, reviewText);
            stmt.executeUpdate();
            System.out.println("Review added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getAverageRating(int productId) {
        String query = "SELECT AVG(rating) AS avg_rating FROM reviews WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Add the following method to insert a new product
    public void addProduct(Product product) {
        String query = "INSERT INTO products (name, description, price, quantity, category_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getQuantity());
            stmt.setInt(5, product.getCategoryId());
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                product.setId(generatedKeys.getInt(1)); // Set the auto-generated ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteProduct(int productId) {
        String query = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // Returns true if the product was deleted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}


class DiscountDAO {
    private Connection connection;

    public DiscountDAO() {
        connection = DatabaseConnection.getConnection();
    }

    public double getDiscountPercentage(String code) {
        String query = "SELECT discount_percentage FROM discounts WHERE code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("discount_percentage") / 100;
            } else {
                System.out.println("Invalid discount code.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}

class ReviewDAO
{
    private Connection connection;

    public ReviewDAO() {
        connection = DatabaseConnection.getConnection();
    }

    public List<String> viewProductReviews(int productId) throws SQLException {
        List<String> reviews = new ArrayList<>();
        String query = "SELECT review_text, rating FROM reviews WHERE product_id = ?";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, productId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String review = "Review: " + rs.getString("review_text") + ", Rating: " + rs.getInt("rating");
            reviews.add(review);
        }

        return reviews;
    }
}

// ShoppingCartSystem class

public class ShoppingCartSystem {
    private static ProductDAO productDAO = new ProductDAO();
    private static DiscountDAO discountDAO = new DiscountDAO();
    private static OrderDAO orderDAO = new OrderDAO();
    private static ReviewDAO reviewDAO = new ReviewDAO();
    private static Scanner scanner = new Scanner(System.in);

    private static Connection connection;
    
    public ShoppingCartSystem() {
        connection = DatabaseConnection.getConnection();
    }
    
    public static void main(String[] args) {
        ShoppingCartSystem shoppingCartSystem = new ShoppingCartSystem();
        int choice;
        do {
            System.out.println("\nE-Commerce System Menu:");
            System.out.println("1. View Products");
            System.out.println("2. Add a New Customer");
            System.out.println("3. Create an Order");
            System.out.println("4. View Order Details");
            System.out.println("5. Add a new Product");
            System.out.println("6. Process Payment");
            System.out.println("7. View Product Reviews");
            System.out.println("8. Add a Review");
            System.out.println("9. View Average Rating of a Product");
            System.out.println("10. Delete Product");
            System.out.println("11. Delete Customer");
            System.out.println("0. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            handleChoice(choice);
        } while (choice != 0);
    }
    
    private static void handleChoice(int choice) {
        switch (choice) {
        case 1 -> viewProducts();
        case 2 -> addCustomer();
        case 3 -> addOrderItem();
        case 4 -> viewOrderDetails();
        case 5 -> addProduct();
        case 6 -> processPayment();
        case 7 -> viewProductReviews();
        case 8 -> addReview();
        case 9-> viewAverageRating();
        case 10-> deleteProduct();
        case 11-> deleteCustomer();
        case 0 -> System.out.println("Exiting the system.");
        default -> System.out.println("Invalid choice. Please try again.");
        }
    }
    
    private static void viewProducts() {
        List<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            System.out.println(product);
        }
    }
    
    private static void addReview() {
        System.out.print("Enter Product ID: ");
        int productId = scanner.nextInt();
        System.out.print("Enter Customer ID: ");
        int customerId = scanner.nextInt();
        System.out.print("Enter Rating (1-5): ");
        int rating = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter Review: ");
        String review = scanner.nextLine();
        productDAO.addReview(productId, customerId, rating, review);
    }
    
    private static void viewAverageRating() {
        System.out.print("Enter Product ID: ");
        int productId = scanner.nextInt();
        double avgRating = productDAO.getAverageRating(productId);
        System.out.println("Average Rating: " + avgRating);
    }

    private static void applyDiscount() {
        System.out.print("Enter Discount Code: ");
        String discountCode = scanner.nextLine();
        double discountPercentage = discountDAO.getDiscountPercentage(discountCode);
        if (discountPercentage > 0) {
            System.out.println("Discount Applied: " + (discountPercentage * 100) + "%");
        } else {
            System.out.println("Invalid discount code.");
        }
    }
    

    private static void processPayment() {
        System.out.print("Enter Order ID: ");
        int orderId = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter Payment Method: ");
        String paymentMethod = scanner.nextLine();
        orderDAO.processPayment(orderId, paymentMethod);
    }
    
    private static void addOrderItem() {
        System.out.print("Enter customer ID: ");
        int customerId = scanner.nextInt();
        
        // Check if customer exists
        String query = "SELECT id FROM customers WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            System.out.print("Enter Product ID: ");
            int productId = scanner.nextInt();
                
            // Check if product exists
            String query1 = "SELECT id FROM products WHERE id = ?";
            try (PreparedStatement stmt1 = connection.prepareStatement(query1)) {
                stmt1.setInt(1, productId);
                ResultSet rs1 = stmt1.executeQuery();
                
                if (rs1.next()) {
                    System.out.print("Enter Quantity: ");
                    int quantity = scanner.nextInt();
                    
                    // Get the available quantity of the product
                    String query2 = "SELECT quantity, price FROM products WHERE id = ?";
                    try (PreparedStatement stmt2 = connection.prepareStatement(query2)) {
                        stmt2.setInt(1, productId);
                        ResultSet rs2 = stmt2.executeQuery();
                        
                        if (rs2.next()) {
                            int availableQuantity = rs2.getInt("quantity");
                            double price = rs2.getDouble("price");
                            
                            // Check if required quantity is available
                            if (availableQuantity < quantity) {
                                System.out.println("The required quantity is not available. Available quantity: " + availableQuantity);
                            } else {
                                // Calculate total price
                                double totalPrice = price * quantity;
                                // Add the order item
                                orderDAO.addOrderItem(customerId, productId, quantity, totalPrice);
                                System.out.println("Order item added successfully!");
                            }
                        }
                    }
                } else {
                        System.out.println("Product not found.");
                    }
            }
        } else {
                System.out.println("Customer not found.");
            }
    } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    

    private static void viewOrderDetails() {
        System.out.print("Enter Order ID: ");
        int orderId = scanner.nextInt();
        List<String> orderItems = orderDAO.getOrderItems(orderId);
        for (String item : orderItems) {
            System.out.println(item);
        }
    }

    private static void addProduct() {
        System.out.print("Enter Product Name: ");
        scanner.nextLine(); // Consume newline
        String name = scanner.nextLine();
        System.out.print("Enter Product Description: ");
        String description = scanner.nextLine();
        System.out.print("Enter Product Price: ");
        double price = scanner.nextDouble();
        System.out.print("Enter Product Quantity: ");
        int quantity = scanner.nextInt();
        System.out.print("Enter Category ID: ");
        int categoryId = scanner.nextInt();
        
        Product product = new Product(0, name, description, price, quantity, categoryId);
        productDAO.addProduct(product);
        System.out.println("Product added successfully with ID: " + product.getId());
    }

    public static void addCustomer() {
        Scanner scanner = new Scanner(System.in);
        
        // Collect customer details
        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Customer Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Customer Address: ");
        String address = scanner.nextLine();
        
        // Create a Customer object
        Customer customer = new Customer(name, email, address);
        
        // Create a CustomerDAO instance to add the customer to the database
        CustomerDAO customerDAO = new CustomerDAO(DatabaseConnection.getConnection());
        
        // Add customer to the database
        customerDAO.addCustomer(customer);
        
        // Print confirmation
        System.out.println("Customer added successfully!");
    }

    public static void viewProductReviews() {
        Scanner scanner = new Scanner(System.in);
    
        try {
            System.out.print("Enter Product ID: ");
            int productId = scanner.nextInt();
    
            List<String> reviews = reviewDAO.viewProductReviews(productId);
            if (reviews.isEmpty()) {
                System.out.println("No reviews available for this product.");
            } else {
                System.out.println("Reviews for Product ID " + productId + ":");
                for (String review : reviews) {
                    System.out.println(review);
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching product reviews: " + e.getMessage());
        }
    }
    


    public static void deleteProduct() {
        System.out.print("Enter Product ID to delete: ");
        int productId = scanner.nextInt();

        ProductDAO productDAO = new ProductDAO();
        boolean result = productDAO.deleteProduct(productId);
        if (result) {
            System.out.println("Product deleted successfully!");
        } else {
            System.out.println("Failed to delete product.");
        }
    }

    public static void deleteCustomer() {
        System.out.print("Enter Customer ID to delete: ");
        int customerId = scanner.nextInt();

        CustomerDAO customerDAO = new CustomerDAO(DatabaseConnection.getConnection());
        boolean result = customerDAO.deleteCustomer(customerId);
        if (result) {
            System.out.println("Customer deleted successfully!");
        } else {
            System.out.println("Failed to delete customer.");
        }
    }
}