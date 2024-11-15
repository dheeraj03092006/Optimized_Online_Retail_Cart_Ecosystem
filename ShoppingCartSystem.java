import java.sql.*;
import java.util.*;

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

    public OrderDAO(Connection connection) {
        this.connection = connection;
    }

    public void createOrder(Order order) {
        String query = "INSERT INTO orders (customer_id, total) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, order.getCustomerId());
            statement.setDouble(2, order.getTotal());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                order.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Failed to create order.");
            e.printStackTrace();
        }
    }

    public void addOrderItem(int orderId, int productId, int quantity) {
        String query = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, orderId);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to add order item.");
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

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', description='" + description + "', price=" + price + ", quantity=" + quantity + '}';
    }
}

// ProductDAO class
class ProductDAO {
    private Connection connection;

    public ProductDAO(Connection connection) {
        this.connection = connection;
    }

    public void addProduct(Product product) {
        String query = "INSERT INTO products (name, description, price, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getQuantity());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                product.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Failed to add product.");
            e.printStackTrace();
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("price"),
                        resultSet.getInt("quantity")
                );
                product.setId(resultSet.getInt("id"));
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve products.");
            e.printStackTrace();
        }
        return products;
    }

    public Product getProductById(int productId) {
        String query = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, productId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Product product = new Product(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("price"),
                        resultSet.getInt("quantity")
                );
                product.setId(resultSet.getInt("id"));
                return product;
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve product by ID.");
            e.printStackTrace();
        }
        return null;
    }

    public void updateProduct(Product product) {
        String query = "UPDATE products SET name = ?, description = ?, price = ?, quantity = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getQuantity());
            statement.setInt(5, product.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update product.");
            e.printStackTrace();
        }
    }
}

// ShoppingCartSystem class
public class ShoppingCartSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/shopping_cart?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Update as per your MySQL username
    private static final String PASS = "Santosh@2006"; // Update as per your MySQL password

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            connection.setAutoCommit(false); // Start transaction

            ProductDAO productDAO = new ProductDAO(connection);
            CustomerDAO customerDAO = new CustomerDAO(connection);
            OrderDAO orderDAO = new OrderDAO(connection);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                // Display menu
                System.out.println("\n==== Shopping Cart System ====");
                System.out.println("1. Add Product");
                System.out.println("2. Add Customer");
                System.out.println("3. Create Order");
                System.out.println("4. View All Products");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                try {
                    switch (choice) {
                        case 1:
                            // Add Product
                            System.out.print("Enter product name: ");
                            String productName = scanner.nextLine();
                            System.out.print("Enter product description: ");
                            String productDescription = scanner.nextLine();
                            System.out.print("Enter product price: ");
                            double productPrice = scanner.nextDouble();
                            System.out.print("Enter product quantity: ");
                            int productQuantity = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            Product product = new Product(productName, productDescription, productPrice, productQuantity);
                            productDAO.addProduct(product);
                            System.out.println("Product added successfully: " + product);
                            break;

                        case 2:
                            // Add Customer
                            System.out.print("Enter customer name: ");
                            String customerName = scanner.nextLine();
                            System.out.print("Enter customer email: ");
                            String customerEmail = scanner.nextLine();
                            System.out.print("Enter customer address: ");
                            String customerAddress = scanner.nextLine();

                            Customer customer = new Customer(customerName, customerEmail, customerAddress);
                            customerDAO.addCustomer(customer);
                            System.out.println("Customer added successfully: " + customer);
                            break;

                        case 3:
                            // Create Order
                            System.out.print("Enter customer ID: ");
                            int customerId = scanner.nextInt();
                            System.out.print("Enter product ID: ");
                            int productId = scanner.nextInt();
                            System.out.print("Enter quantity: ");
                            int orderQuantity = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            Product selectedProduct = productDAO.getProductById(productId);
                            if (selectedProduct == null) {
                                System.out.println("Product not found.");
                                break;
                            }

                            if (orderQuantity > selectedProduct.getQuantity()) {
                                System.out.println("Insufficient stock for " + selectedProduct.getName());
                                break;
                            }

                            double orderTotal = selectedProduct.getPrice() * orderQuantity;

                            // Update product quantity
                            int updatedQuantity = selectedProduct.getQuantity() - orderQuantity;
                            selectedProduct.setQuantity(updatedQuantity);
                            productDAO.updateProduct(selectedProduct);

                            // Create order and add order item
                            Order order = new Order(customerId, orderTotal);
                            orderDAO.createOrder(order);
                            orderDAO.addOrderItem(order.getId(), productId, orderQuantity);

                            System.out.println("Order created successfully: " + order);
                            break;

                        case 4:
                            // View All Products
                            System.out.println("\nProducts in inventory:");
                            productDAO.getAllProducts().forEach(System.out::println);
                            break;

                        case 5:
                            // Exit
                            System.out.println("Exiting the system. Goodbye!");
                            return;

                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                    connection.commit(); // Commit the transaction
                } catch (SQLException e) {
                    System.err.println("Operation failed. Rolling back transaction...");
                    connection.rollback(); // Rollback on error
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
