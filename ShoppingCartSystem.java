package Optimized_Online_Retail_Cart_Ecosystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;



public class ShoppingCartSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/shopping_cart?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Change according to your MySQL username
    private static final String PASS = "Santosh@2006"; // Change according to your MySQL password

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            connection.setAutoCommit(false); // Start transaction

            // Example usage
            ProductDAO productDAO = new ProductDAO(connection);
            CustomerDAO customerDAO = new CustomerDAO(connection);
            OrderDAO orderDAO = new OrderDAO(connection);

            try {
                // Add a product
                Product product = new Product("Laptop", "A high-performance laptop", 1200.00, 10);
                productDAO.addProduct(product);

                // Add a customer
                Customer customer = new Customer("John Doe", "john@example.com", "123 Main St");
                customerDAO.addCustomer(customer);

                // Create an order
                Order order = new Order(customer.getId(), 1200.00);
                orderDAO.createOrder(order);

                // Add order items
                orderDAO.addOrderItem(order.getId(), product.getId(), 1);

                // Commit transaction
                connection.commit();
            } catch (SQLException e) {
                System.err.println("Transaction failed, rolling back changes.");
                connection.rollback(); // Rollback changes on error
                e.printStackTrace();
            }

            // List products
            List<Product> products = productDAO.getAllProducts();
            products.forEach(System.out::println);

        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}