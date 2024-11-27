import java.sql.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<String> getOrderDetailsByCustomerId(int customerId) {
        List<String> orderDetails = new ArrayList<>();
        String sql = "{ CALL ViewOrdersByCustomerId(?) }"; // Procedure call
        
        try (Connection conn = DatabaseConnection.getConnection(); 
             CallableStatement stmt = conn.prepareCall(sql)) {
             
            stmt.setInt(1, customerId); // Set the customer ID
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                // Retrieve and format the details
                String detail = String.format(
                    "Order ID: %d, Customer: %s, Email: %s, Product: %s, Quantity: %d, Total Price: %.2f",
                    rs.getInt("Order_ID"),
                    rs.getString("Customer_Name"),
                    rs.getString("Customer_Email"),
                    rs.getString("Product_Name"),
                    rs.getInt("Quantity"),
                    rs.getDouble("Total_Price")
                );
                orderDetails.add(detail);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exceptions
        }
        
        return orderDetails;
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

    private boolean validatePaymentDetails(String paymentMethod, Map<String, String> paymentDetails) {
        switch (paymentMethod.toLowerCase()) {
            case "card":
                return paymentDetails.containsKey("cardNumber") &&
                       paymentDetails.get("cardNumber").matches("\\d{16}") && // 16-digit card number
                       paymentDetails.containsKey("expiryDate") &&
                       paymentDetails.get("expiryDate").matches("(0[1-9]|1[0-2])/\\d{2}"); // MM/YY format
    
            case "upi":
                return paymentDetails.containsKey("upiId") &&
                       paymentDetails.get("upiId").matches("[a-zA-Z0-9.-_]+@[a-zA-Z]+"); // Basic UPI ID format
    
            case "netbanking":
                return paymentDetails.containsKey("bankName") &&
                       paymentDetails.get("bankName").length() > 2 &&
                       paymentDetails.containsKey("accountNumber") &&
                       paymentDetails.get("accountNumber").matches("\\d+"); // Numeric account number
    
            case "wallet":
                return paymentDetails.containsKey("walletId") &&
                       paymentDetails.get("walletId").length() > 5;
    
            default:
                return false; // Invalid payment method
        }
    }
    
    public void processPaymentForCustomer(int customerId, String paymentMethod, Map<String, String> paymentDetails) {
        System.out.println("Processing payment for Customer ID: " + customerId);
    
        // Validate payment details
        if (!validatePaymentDetails(paymentMethod, paymentDetails)) {
            System.out.println("Invalid payment details for method: " + paymentMethod);
            return;
        }
    
        String totalAmountQuery = "SELECT SUM(price) AS totalAmount FROM order_items WHERE customer_id = ?";
        String deleteOrdersQuery = "DELETE FROM order_items WHERE customer_id = ?";
        String insertPaymentQuery = "INSERT INTO payments (customer_id, payment_method, payment_status, transaction_date, payment_details) VALUES (?, ?, ?, ?, ?)";
    
        try (PreparedStatement totalStmt = connection.prepareStatement(totalAmountQuery);
             PreparedStatement deleteStmt = connection.prepareStatement(deleteOrdersQuery);
             PreparedStatement paymentStmt = connection.prepareStatement(insertPaymentQuery)) {
    
            // Calculate total amount
            totalStmt.setInt(1, customerId);
            ResultSet rs = totalStmt.executeQuery();
            double totalAmount = 0;
            if (rs.next()) {
                totalAmount = rs.getDouble("totalAmount");
            }
    
            if (totalAmount == 0) {
                System.out.println("No orders found for Customer ID: " + customerId);
                return;
            }
    
            System.out.println("Total Amount for Customer ID " + customerId + ": " + totalAmount);
    
            // Insert payment record
            paymentStmt.setInt(1, customerId);
            paymentStmt.setString(2, paymentMethod);
            paymentStmt.setString(3, "Completed");
            paymentStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            paymentStmt.setString(5, createJsonFromMap(paymentDetails)); // Convert Map to JSON
            paymentStmt.executeUpdate();
    
            // Delete orders after successful payment
            deleteStmt.setInt(1, customerId);
            deleteStmt.executeUpdate();
    
            System.out.println("Payment of " + totalAmount + " for Customer ID " + customerId + " processed successfully.");
        } catch (SQLException e) {
            System.err.println("Error processing payment for Customer ID: " + customerId);
            e.printStackTrace();
        }
    }
    
    private String createJsonFromMap(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\",");
        }
        if (json.length() > 1) {
            json.deleteCharAt(json.length() - 1); // Remove trailing comma
        }
        json.append("}");
        return json.toString();
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


public class ShoppingCartSystem extends JFrame {
    private static ProductDAO productDAO = new ProductDAO();
    private static DiscountDAO discountDAO = new DiscountDAO();
    private static OrderDAO orderDAO = new OrderDAO();
    private static ReviewDAO reviewDAO = new ReviewDAO();
    private static CustomerDAO customerDAO = new CustomerDAO(DatabaseConnection.getConnection());

    public ShoppingCartSystem() {
        setTitle("E-Commerce System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");
        menuBar.add(menu);
        
        // Add menu items
        JMenuItem viewProductsItem = new JMenuItem("View Products");
        JMenuItem addCustomerItem = new JMenuItem("Add Customer");
        JMenuItem createOrderItem = new JMenuItem("Create Order");
        JMenuItem viewOrderDetailsItem = new JMenuItem("View Order Details");
        JMenuItem addProductItem = new JMenuItem("Add Product");
        JMenuItem processPaymentItem = new JMenuItem("Process Payment");
        JMenuItem viewProductReviewsItem = new JMenuItem("View Product Reviews");
        JMenuItem addReviewItem = new JMenuItem("Add Review");
        JMenuItem viewAverageRatingItem = new JMenuItem("View Average Rating");
        JMenuItem deleteProductItem = new JMenuItem("Delete Product");
        JMenuItem deleteCustomerItem = new JMenuItem("Delete Customer");
        JMenuItem exitItem = new JMenuItem("Exit");

        menu.add(viewProductsItem);
        menu.add(addCustomerItem);
        menu.add(createOrderItem);
        menu.add(viewOrderDetailsItem);
        menu.add(addProductItem);
        menu.add(processPaymentItem);
        menu.add(viewProductReviewsItem);
        menu.add(addReviewItem);
        menu.add(viewAverageRatingItem);
        menu.add(deleteProductItem);
        menu.add(deleteCustomerItem);
        menu.add(exitItem);

        setJMenuBar(menuBar);

        // Add action listeners for menu items
        viewProductsItem.addActionListener(e -> viewProducts());
        addCustomerItem.addActionListener(e -> addCustomer());
        createOrderItem.addActionListener(e -> addOrderItem());
        viewOrderDetailsItem.addActionListener(e -> viewOrderDetails());
        addProductItem.addActionListener(e -> addProduct());
        processPaymentItem.addActionListener(e -> processPayment());
        viewProductReviewsItem.addActionListener(e -> viewProductReviews());
        addReviewItem.addActionListener(e -> addReview());
        viewAverageRatingItem.addActionListener(e -> viewAverageRating());
        deleteProductItem.addActionListener(e -> deleteProduct());
        deleteCustomerItem.addActionListener(e -> deleteCustomer());
        exitItem.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void viewProducts() {
        List<Product> products = productDAO.getAllProducts();
        StringBuilder productList = new StringBuilder("Products:\n");
        for (Product product : products) {
            productList.append(product.toString()).append("\n");
        }
        JOptionPane.showMessageDialog(this, productList.toString(), "View Products", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addCustomer() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField addressField = new JTextField();

        Object[] message = {
            "Name:", nameField,
            "Email:", emailField,
            "Address:", addressField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Customer", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            Customer customer = new Customer(nameField.getText(), emailField.getText(), addressField.getText());
            customerDAO.addCustomer(customer);
            JOptionPane.showMessageDialog(this, "Customer added successfully!");
        }
    }

    private void addOrderItem() {
        JTextField customerIdField = new JTextField();
        JTextField productIdField = new JTextField();
        JTextField quantityField = new JTextField();

        Object[] message = {
            "Customer ID:", customerIdField,
            "Product ID:", productIdField,
            "Quantity:", quantityField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Order Item", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int customerId = Integer.parseInt (customerIdField.getText());
            int productId = Integer.parseInt(productIdField.getText());
            int quantity = Integer.parseInt(quantityField.getText());

            // Get the available quantity of the product
            String query = "SELECT quantity, price FROM products WHERE id = ?";
            try (PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(query)) {
                stmt.setInt(1, productId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int availableQuantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");

                    // Check if required quantity is available
                    if (availableQuantity < quantity) {
                        JOptionPane.showMessageDialog(this, "The required quantity is not available. Available quantity: " + availableQuantity);
                    } else {
                        double totalPrice = price * quantity;
                        orderDAO.addOrderItem(customerId, productId, quantity, totalPrice);
                        JOptionPane.showMessageDialog(this, "Order item added successfully!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found.");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void viewOrderDetails() {
        JTextField customerIdField = new JTextField();
        Object[] message = {
            "Customer ID:", customerIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "View Order Details", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int customerId = Integer.parseInt(customerIdField.getText());
            List<String> orderDetails = orderDAO.getOrderDetailsByCustomerId(customerId);

            if (orderDetails.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No orders found for the given Customer ID.");
            } else {
                StringBuilder details = new StringBuilder("Order Details:\n");
                for (String detail : orderDetails) {
                    details.append(detail).append("\n");
                }
                JOptionPane.showMessageDialog(this, details.toString(), "Order Details", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void addProduct() {
        JTextField nameField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField categoryIdField = new JTextField();

        Object[] message = {
            "Name:", nameField,
            "Description:", descriptionField,
            "Price:", priceField,
            "Quantity:", quantityField,
            "Category ID:", categoryIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String description = descriptionField.getText();
            double price = Double.parseDouble(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            int categoryId = Integer.parseInt(categoryIdField.getText());

            Product product = new Product(0, name, description, price, quantity, categoryId);
            productDAO.addProduct(product);
            JOptionPane.showMessageDialog(this, "Product added successfully with ID: " + product.getId());
        }
    }

    private void processPayment() {
        JTextField customerIdField = new JTextField();
        JTextField paymentMethodField = new JTextField();

        Object[] message = {
            "Customer ID:", customerIdField,
            "Payment Method (Card/UPI/NetBanking/Wallet):", paymentMethodField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Process Payment", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int customerId = Integer.parseInt(customerIdField.getText());
            String paymentMethod = paymentMethodField.getText();

            Map<String, String> paymentDetails = new HashMap<>();
            switch (paymentMethod.toLowerCase()) {
                case "card":
                    paymentDetails.put("cardNumber", JOptionPane.showInputDialog("Enter Card Number:"));
                    paymentDetails.put("cardHolderName", JOptionPane.showInputDialog("Enter Card Holder Name:"));
                    paymentDetails.put("expiryDate", JOptionPane.showInputDialog("Enter Expiry Date (MM/YY):"));
                    break;

                case "upi":
                    paymentDetails.put("upiId", JOptionPane.showInputDialog("Enter UPI ID:"));
                    break;

                case "netbanking":
                    paymentDetails.put("bankName", JOptionPane.showInputDialog("Enter Bank Name:"));
                    paymentDetails.put("accountNumber", JOptionPane.showInputDialog("Enter Account Number:"));
                    break;

                case " wallet":
                    paymentDetails.put("walletId", JOptionPane.showInputDialog("Enter Wallet ID:"));
                    break;

                default:
                    JOptionPane.showMessageDialog(this, "Invalid payment method.");
                    return;
            }

            orderDAO.processPaymentForCustomer(customerId, paymentMethod, paymentDetails);
            JOptionPane.showMessageDialog(this, "Payment processed successfully!");
        }
    }

    private void viewProductReviews() {
        JTextField productIdField = new JTextField();
        Object[] message = {
            "Product ID:", productIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "View Product Reviews", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int productId = Integer.parseInt(productIdField.getText());
            try {
                List<String> reviews = reviewDAO.viewProductReviews(productId);
                if (reviews.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No reviews available for this product.");
                } else {
                    StringBuilder reviewList = new StringBuilder("Reviews for Product ID " + productId + ":\n");
                    for (String review : reviews) {
                        reviewList.append(review).append("\n");
                    }
                    JOptionPane.showMessageDialog(this, reviewList.toString(), "Product Reviews", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error fetching product reviews: " + e.getMessage());
            }
        }
    }

    private void addReview() {
        JTextField productIdField = new JTextField();
        JTextField customerIdField = new JTextField();
        JTextField ratingField = new JTextField();
        JTextField reviewTextField = new JTextField();

        Object[] message = {
            "Product ID:", productIdField,
            "Customer ID:", customerIdField,
            "Rating (1-5):", ratingField,
            "Review Text:", reviewTextField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Review", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int productId = Integer.parseInt(productIdField.getText());
            int customerId = Integer.parseInt(customerIdField.getText());
            int rating = Integer.parseInt(ratingField.getText());
            String reviewText = reviewTextField.getText();

            productDAO.addReview(productId, customerId, rating, reviewText);
            JOptionPane.showMessageDialog(this, "Review added successfully!");
        }
    }

    private void viewAverageRating() {
        JTextField productIdField = new JTextField();
        Object[] message = {
            "Product ID:", productIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "View Average Rating", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int productId = Integer.parseInt(productIdField.getText());
            double avgRating = productDAO.getAverageRating(productId);
            JOptionPane.showMessageDialog(this, "Average Rating: " + avgRating);
        }
    }

    private void deleteProduct() {
        JTextField productIdField = new JTextField();
        Object[] message = {
            "Product ID to delete:", productIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Delete Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int productId = Integer.parseInt(productIdField.getText());
            boolean result = productDAO.deleteProduct(productId);
            if (result) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.");
            }
        }
    }

    private void deleteCustomer() {
        JTextField customerIdField = new JTextField();
        Object[] message = {
            "Customer ID to delete:", customerIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Delete Customer", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            int customerId = Integer.parseInt(customerIdField.getText());
            boolean result = customerDAO.deleteCustomer(customerId);
            if (result) {
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete customer.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShoppingCartSystem::new);
    }
}


// public class ShoppingCartSystem {
//     private static ProductDAO productDAO = new ProductDAO();
//     private static DiscountDAO discountDAO = new DiscountDAO();
//     private static OrderDAO orderDAO = new OrderDAO();
//     private static ReviewDAO reviewDAO = new ReviewDAO();
//     private static Scanner scanner = new Scanner(System.in);

//     private static Connection connection;
    
//     public ShoppingCartSystem() {
//         connection = DatabaseConnection.getConnection();
//     }
    
//     public static void main(String[] args) {
//         ShoppingCartSystem shoppingCartSystem = new ShoppingCartSystem();
//         int choice;
//         do {
//             System.out.println("\nE-Commerce System Menu:");
//             System.out.println("1. View Products");
//             System.out.println("2. Add a New Customer");
//             System.out.println("3. Create an Order");
//             System.out.println("4. View Order Details");
//             System.out.println("5. Add a new Product");
//             System.out.println("6. Process Payment");
//             System.out.println("7. View Product Reviews");
//             System.out.println("8. Add a Review");
//             System.out.println("9. View Average Rating of a Product");
//             System.out.println("10. Delete Product");
//             System.out.println("11. Delete Customer");
//             System.out.println("0. Exit");
//             System.out.print("Enter your choice: ");
//             choice = scanner.nextInt();
//             handleChoice(choice);
//         } while (choice != 0);
//     }
    
//     private static void handleChoice(int choice) {
//         switch (choice) {
//         case 1 -> viewProducts();
//         case 2 -> addCustomer();
//         case 3 -> addOrderItem();
//         case 4 -> viewOrderDetails();
//         case 5 -> addProduct();
//         case 6 -> processPayment();
//         case 7 -> viewProductReviews();
//         case 8 -> addReview();
//         case 9-> viewAverageRating();
//         case 10-> deleteProduct();
//         case 11-> deleteCustomer();
//         case 0 -> System.out.println("Exiting the system.");
//         default -> System.out.println("Invalid choice. Please try again.");
//         }
//     }
    
//     private static void viewProducts() {
//         List<Product> products = productDAO.getAllProducts();
//         for (Product product : products) {
//             System.out.println(product);
//         }
//     }
    
//     private static void addReview() {
//         System.out.print("Enter Product ID: ");
//         int productId = scanner.nextInt();
//         System.out.print("Enter Customer ID: ");
//         int customerId = scanner.nextInt();
//         System.out.print("Enter Rating (1-5): ");
//         int rating = scanner.nextInt();
//         scanner.nextLine(); // Consume newline
//         System.out.print("Enter Review: ");
//         String review = scanner.nextLine();
//         productDAO.addReview(productId, customerId, rating, review);
//     }
    
//     private static void viewAverageRating() {
//         System.out.print("Enter Product ID: ");
//         int productId = scanner.nextInt();
//         double avgRating = productDAO.getAverageRating(productId);
//         System.out.println("Average Rating: " + avgRating);
//     }

//     private static void applyDiscount() {
//         System.out.print("Enter Discount Code: ");
//         String discountCode = scanner.nextLine();
//         double discountPercentage = discountDAO.getDiscountPercentage(discountCode);
//         if (discountPercentage > 0) {
//             System.out.println("Discount Applied: " + (discountPercentage * 100) + "%");
//         } else {
//             System.out.println("Invalid discount code.");
//         }
//     }
    

//     private static void processPayment() {
//         System.out.print("Enter Customer ID: ");
//         int customerId = scanner.nextInt();
//         scanner.nextLine(); // Consume newline
//         System.out.print("Enter Payment Method (Card/UPI/NetBanking/Wallet): ");
//         String paymentMethod = scanner.nextLine();
    
//         Map<String, String> paymentDetails = new HashMap<>();
//         switch (paymentMethod.toLowerCase()) {
//             case "card":
//                 System.out.print("Enter Card Number: ");
//                 String cardNumber = scanner.nextLine();
//                 System.out.print("Enter Card Holder Name: ");
//                 String cardHolderName = scanner.nextLine();
//                 System.out.print("Enter Expiry Date (MM/YY): ");
//                 String expiryDate = scanner.nextLine();
//                 paymentDetails.put("cardNumber", cardNumber);
//                 paymentDetails.put("cardHolderName", cardHolderName);
//                 paymentDetails.put("expiryDate", expiryDate);
//                 break;
    
//             case "upi":
//                 System.out.print("Enter UPI ID: ");
//                 String upiId = scanner.nextLine();
//                 paymentDetails.put("upiId", upiId);
//                 break;
    
//             case "netbanking":
//                 System.out.print("Enter Bank Name: ");
//                 String bankName = scanner.nextLine();
//                 System.out.print("Enter Account Number: ");
//                 String accountNumber = scanner.nextLine();
//                 paymentDetails.put("bankName", bankName);
//                 paymentDetails.put("accountNumber", accountNumber);
//                 break;
    
//             case "wallet":
//                 System.out.print("Enter Wallet ID: ");
//                 String walletId = scanner.nextLine();
//                 paymentDetails.put("walletId", walletId);
//                 break;
    
//             default:
//                 System.out.println("Invalid payment method.");
//                 return;
//         }
    
//         orderDAO.processPaymentForCustomer(customerId, paymentMethod, paymentDetails);
//     }    
    
//     private static void addOrderItem() {
//         System.out.print("Enter customer ID: ");
//         int customerId = scanner.nextInt();
        
//         // Check if customer exists
//         String query = "SELECT id FROM customers WHERE id = ?";
//         try (PreparedStatement stmt = connection.prepareStatement(query)) {
//         stmt.setInt(1, customerId);
//         ResultSet rs = stmt.executeQuery();
        
//         if (rs.next()) {
//             System.out.print("Enter Product ID: ");
//             int productId = scanner.nextInt();
                
//             // Check if product exists
//             String query1 = "SELECT id FROM products WHERE id = ?";
//             try (PreparedStatement stmt1 = connection.prepareStatement(query1)) {
//                 stmt1.setInt(1, productId);
//                 ResultSet rs1 = stmt1.executeQuery();
                
//                 if (rs1.next()) {
//                     System.out.print("Enter Quantity: ");
//                     int quantity = scanner.nextInt();
                    
//                     // Get the available quantity of the product
//                     String query2 = "SELECT quantity, price FROM products WHERE id = ?";
//                     try (PreparedStatement stmt2 = connection.prepareStatement(query2)) {
//                         stmt2.setInt(1, productId);
//                         ResultSet rs2 = stmt2.executeQuery();
                        
//                         if (rs2.next()) {
//                             int availableQuantity = rs2.getInt("quantity");
//                             double price = rs2.getDouble("price");
                            
//                             // Check if required quantity is available
//                             if (availableQuantity < quantity) {
//                                 System.out.println("The required quantity is not available. Available quantity: " + availableQuantity);
//                             } else {
//                                 // Calculate total price
//                                 double totalPrice = price * quantity;
//                                 // Add the order item
//                                 orderDAO.addOrderItem(customerId, productId, quantity, totalPrice);
//                                 System.out.println("Order item added successfully!");
//                             }
//                         }
//                     }
//                 } else {
//                         System.out.println("Product not found.");
//                     }
//             }
//         } else {
//                 System.out.println("Customer not found.");
//             }
//     } catch (SQLException e) {
//             System.out.println("Error: " + e.getMessage());
//         }
//     }
    

//     private static void viewOrderDetails() {
//         System.out.print("Enter Customer ID: ");
//         int customerId = scanner.nextInt();
        
//         // Call the procedure to fetch order details for the given customer
//         List<String> orderDetails = orderDAO.getOrderDetailsByCustomerId(customerId);
        
//         if (orderDetails.isEmpty()) {
//             System.out.println("No orders found for the given Customer ID.");
//         } else {
//             System.out.println("Order Details:");
//             for (String detail : orderDetails) {
//                 System.out.println(detail);
//             }
//         }
//     }
    
//     private static void addProduct() {
//         System.out.print("Enter Product Name: ");
//         scanner.nextLine(); // Consume newline
//         String name = scanner.nextLine();
//         System.out.print("Enter Product Description: ");
//         String description = scanner.nextLine();
//         System.out.print("Enter Product Price: ");
//         double price = scanner.nextDouble();
//         System.out.print("Enter Product Quantity: ");
//         int quantity = scanner.nextInt();
//         System.out.print("Enter Category ID: ");
//         int categoryId = scanner.nextInt();
        
//         Product product = new Product(0, name, description, price, quantity, categoryId);
//         productDAO.addProduct(product);
//         System.out.println("Product added successfully with ID: " + product.getId());
//     }

//     public static void addCustomer() {
//         Scanner scanner = new Scanner(System.in);
        
//         // Collect customer details
//         System.out.print("Enter Customer Name: ");
//         String name = scanner.nextLine();
//         System.out.print("Enter Customer Email: ");
//         String email = scanner.nextLine();
//         System.out.print("Enter Customer Address: ");
//         String address = scanner.nextLine();
        
//         // Create a Customer object
//         Customer customer = new Customer(name, email, address);
        
//         // Create a CustomerDAO instance to add the customer to the database
//         CustomerDAO customerDAO = new CustomerDAO(DatabaseConnection.getConnection());
        
//         // Add customer to the database
//         customerDAO.addCustomer(customer);
        
//         // Print confirmation
//         System.out.println("Customer added successfully!");
//     }

//     public static void viewProductReviews() {
//         Scanner scanner = new Scanner(System.in);
    
//         try {
//             System.out.print("Enter Product ID: ");
//             int productId = scanner.nextInt();
    
//             List<String> reviews = reviewDAO.viewProductReviews(productId);
//             if (reviews.isEmpty()) {
//                 System.out.println("No reviews available for this product.");
//             } else {
//                 System.out.println("Reviews for Product ID " + productId + ":");
//                 for (String review : reviews) {
//                     System.out.println(review);
//                 }
//             }
//         } catch (Exception e) {
//             System.out.println("Error fetching product reviews: " + e.getMessage());
//         }
//     }
    


//     public static void deleteProduct() {
//         System.out.print("Enter Product ID to delete: ");
//         int productId = scanner.nextInt();

//         ProductDAO productDAO = new ProductDAO();
//         boolean result = productDAO.deleteProduct(productId);
//         if (result) {
//             System.out.println("Product deleted successfully!");
//         } else {
//             System.out.println("Failed to delete product.");
//         }
//     }

//     public static void deleteCustomer() {
//         System.out.print("Enter Customer ID to delete: ");
//         int customerId = scanner.nextInt();

//         CustomerDAO customerDAO = new CustomerDAO(DatabaseConnection.getConnection());
//         boolean result = customerDAO.deleteCustomer(customerId);
//         if (result) {
//             System.out.println("Customer deleted successfully!");
//         } else {
//             System.out.println("Failed to delete customer.");
//         }
//     }
// }