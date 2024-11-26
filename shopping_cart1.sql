CREATE DATABASE shopping_cart1;

USE shopping_cart1;

-- Categories Table
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Products Table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    category_id INT,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Customers Table
CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    address TEXT
);

-- Wishlists Table
CREATE TABLE wishlists (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Payments Table
CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'Pending',
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    card_number VARCHAR(16), -- For credit/debit card payments
    upi_id VARCHAR(50), -- For UPI payments
    bank_name VARCHAR(50), -- For net banking
    wallet_id VARCHAR(50), -- For wallet payments
    payment_details JSON,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);


-- Reviews Table
CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    customer_id INT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    review_text TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Discounts Table
CREATE TABLE discounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    discount_percentage DECIMAL(5, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
);

-- Order Items Table
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

DELIMITER $$

CREATE PROCEDURE ViewOrdersByCustomerId(IN customer_id INT)
BEGIN
    SELECT 
        oi.id AS Order_ID,
        c.name AS Customer_Name,
        c.email AS Customer_Email,
        p.name AS Product_Name,
        oi.quantity AS Quantity,
        oi.price AS Total_Price,
        p.price AS Product_Price,
        p.description AS Product_Description
    FROM 
        order_items oi
    JOIN 
        customers c ON oi.customer_id = c.id
    JOIN 
        products p ON oi.product_id = p.id
    WHERE 
        oi.customer_id = customer_id
    ORDER BY 
        oi.customer_id;
END$$

DELIMITER ;

DELIMITER $$

CREATE PROCEDURE DeleteOrdersAfterPayment(IN customer_id INT)
BEGIN
    -- Check if the payment is successful for the given customer_id
    IF EXISTS (
        SELECT 1 
        FROM payments 
        WHERE customer_id = customer_id AND payment_status = 'Successful'
    ) THEN
        -- Delete the orders from order_items table
        DELETE FROM order_items 
        WHERE customer_id = customer_id;
    ELSE
        -- If no successful payment is found, signal the issue
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'No successful payment found for the provided customer ID.';
    END IF;
END$$

DELIMITER ;
