-- Tạo cơ sở dữ liệu
CREATE DATABASE bookstore;

-- Kết nối đến cơ sở dữ liệu vừa tạo
\c bookstore

-- Tạo bảng Categories
CREATE TABLE categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100)
);

-- Tạo bảng Books
CREATE TABLE books (
    book_id SERIAL PRIMARY KEY,
    title VARCHAR(255),
    author VARCHAR(255),
    publisher VARCHAR(255),
    publication_year SMALLINT,
    category_id INT,
    price DECIMAL(10, 2),
    stock_quantity INT,
    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories (category_id)
);

-- Tạo bảng Users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(5) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    status VARCHAR(10) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_role CHECK (role IN ('KH', 'Nvien', 'Qly')),
    CONSTRAINT chk_users_status CHECK (status IN ('Active', 'Lock'))
);

-- Tạo bảng Book_Images
CREATE TABLE book_images (
    image_id SERIAL PRIMARY KEY,
    book_id INT,
    image_url VARCHAR(255),
    description VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_images_book FOREIGN KEY (book_id) REFERENCES books (book_id)
);

-- Tạo bảng Inventory_Transactions
CREATE TABLE inventory_transactions (
    transaction_id SERIAL PRIMARY KEY,
    book_id INT,
    transaction_type VARCHAR(10),
    quantity INT,
    transaction_date TIMESTAMP,
    price_at_transaction DECIMAL(10, 2),
    user_id INT,
    CONSTRAINT fk_inventory_transactions_book FOREIGN KEY (book_id) REFERENCES books (book_id),
    CONSTRAINT fk_inventory_transactions_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('Nhập', 'Xuất'))
);

-- Tạo bảng Orders
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT,
    order_date TIMESTAMP,
    total_amount DECIMAL(10, 2),
    status VARCHAR(20),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT chk_orders_status CHECK (status IN ('Đang xử lý', 'Đã giao', 'Đã hủy'))
);

-- Tạo bảng Order_Details
CREATE TABLE order_details (
    order_detail_id SERIAL PRIMARY KEY,
    order_id INT,
    book_id INT,
    quantity INT,
    price_at_order DECIMAL(10, 2),
    CONSTRAINT fk_order_details_order FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT fk_order_details_book FOREIGN KEY (book_id) REFERENCES books (book_id)
);

-- Tạo bảng Payments
CREATE TABLE payments (
    payment_id SERIAL PRIMARY KEY,
    order_id INT,
    payment_date TIMESTAMP,
    amount DECIMAL(10, 2),
    payment_method VARCHAR(50),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (order_id)
);

-- Tạo bảng Shopping_Cart
CREATE TABLE shopping_cart (
    cart_id SERIAL PRIMARY KEY,
    user_id INT,
    book_id INT,
    quantity INT,
    CONSTRAINT fk_shopping_cart_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_shopping_cart_book FOREIGN KEY (book_id) REFERENCES books (book_id)
);

CREATE TABLE invoices (
    invoice_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    invoice_date TIMESTAMP NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    shipping_address TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Chèn dữ liệu vào bảng Categories
INSERT INTO categories (category_id, category_name) VALUES
(1, 'Văn học'),
(2, 'Khoa học'),
(3, 'Truyện tranh'),
(4, 'Kinh tế'),
(5, 'Lịch sử'),
(6, 'Thiếu nhi');

-- Chèn dữ liệu vào bảng Books
INSERT INTO books (book_id, title, author, publisher, publication_year, category_id, price, stock_quantity) VALUES
(1, 'Tắt Đèn', 'Ngô Tất Tố', 'NXB Văn Học', 1939, 1, 50000.00, 10),
(2, 'Số Đỏ', 'Vũ Trọng Phụng', 'NXB Văn Học', 1936, 1, 60000.00, 10),
(3, 'Lão Hạc', 'Nam Cao', 'NXB Văn Học', 1943, 1, 45000.00, 10),
(4, 'Chí Phèo', 'Nam Cao', 'NXB Văn Học', 1941, 1, 55000.00, 10),
(5, 'Đời Mưa Gió', 'Nhất Linh', 'NXB Văn Học', 1936, 1, 70000.00, 10),
(6, 'Vũ Trụ', 'Carl Sagan', 'NXB Khoa Học', 1980, 2, 120000.00, 10),
(7, 'Lược Sử Thời Gian', 'Stephen Hawking', 'NXB Khoa Học', 1988, 2, 130000.00, 10),
(8, 'Gen Tự Mãn', 'Richard Dawkins', 'NXB Khoa Học', 1976, 2, 110000.00, 10),
(9, 'Hạt Của Chúa', 'Leon Lederman', 'NXB Khoa Học', 1993, 2, 140000.00, 10),
(10, 'Nguồn Gốc Loài', 'Charles Darwin', 'NXB Khoa Học', 1859, 2, 150000.00, 10),
(11, 'Naruto', 'Masashi Kishimoto', 'NXB Kim Đồng', 1999, 3, 30000.00, 10),
(12, 'One Piece', 'Eiichiro Oda', 'NXB Kim Đồng', 1997, 3, 35000.00, 10),
(13, 'Dragon Ball', 'Akira Toriyama', 'NXB Kim Đồng', 1984, 3, 32000.00, 10),
(14, 'Doraemon', 'Fujiko F. Fujio', 'NXB Kim Đồng', 1970, 3, 25000.00, 10),
(15, 'Conan', 'Gosho Aoyama', 'NXB Kim Đồng', 1994, 3, 28000.00, 10),
(26, 'Dế Mèn Phiêu Lưu Ký', 'Tô Hoài', 'NXB Thiếu Nhi', 1941, 6, 40000.00, 10),
(27, 'Cây Khế', 'Truyện Dân Gian', 'NXB Thiếu Nhi', 2000, 6, 35000.00, 10),
(28, 'Chú Bé Rừng Xanh', 'Rudyard Kipling', 'NXB Thiếu Nhi', 1894, 6, 45000.00, 10),
(29, 'Hoàng Tử Bé', 'Antoine de Saint-Exupéry', 'NXB Thiếu Nhi', 1943, 6, 50000.00, 10),
(30, 'Alice Ở Xứ Sở Thần Tiên', 'Lewis Carroll', 'NXB Thiếu Nhi', 1865, 6, 48000.00, 10);

-- Chèn dữ liệu vào bảng Users
INSERT INTO users (user_id, username, password, role, email, phone, address, status, created_at) VALUES
(1, 'admin', 'admin123', 'Qly', 'admin@bookstore.com', '0901234567', '123 Đường Quản Lý, TP.HCM', 'Active', NULL),
(2, 'nv1', 'nv123', 'Nvien', 'nv1@bookstore.com', '0912345678', '456 Đường Nhân Viên, TP.HCM', 'Active', NULL),
(3, 'nv2', 'nv123', 'Nvien', 'nv2@bookstore.com', '0923456789', '789 Đường Nhân Viên, TP.HCM', 'Active', NULL),
(4, 'nv3', 'nv123', 'Nvien', 'nv3@bookstore.com', '0934567890', '101 Đường Nhân Viên, TP.HCM', 'Active', NULL),
(5, 'kh1', 'kh123', 'KH', 'kh1@gmail.com', '0945678901', '111 Đường Khách Hàng, TP.HCM', 'Active', NULL),
(6, 'kh2', 'kh123', 'KH', 'kh2@gmail.com', '0956789012', '222 Đường Khách Hàng, TP.HCM', 'Active', NULL),
(7, 'kh3', 'kh123', 'KH', 'kh3@gmail.com', '0967890123', '333 Đường Khách Hàng, TP.HCM', 'Active', NULL),
(8, 'kh4', 'kh123', 'KH', 'kh4@gmail.com', '0978901234', '444 Đường Khách Hàng, TP.HCM', 'Active', NULL),
(9, 'kh5', 'kh123', 'KH', 'kh5@gmail.com', '0989012345', '555 Đường Khách Hàng, TP.HCM', 'Active', NULL),
(10, 'kh6', 'kh123', 'KH', 'kh6@gmail.com', '0990123456', '666 Đường Khách Hàng, TP.HCM', 'Active', NULL),
(11, 'yuu', '123', 'Qly', 'vonghia9a5@gmail.com', '0372858948', 'Tân Thới Nhì Hóc Môn Thành Phố HỒ CHÍ MINH', 'Active', '2025-04-01 19:14:00.070'),
(13, 'yuu1', '123', 'Qly', 'vonghia9a6@gmail.com', '0372858948', 'Tân Thới Nhì Hóc Môn Thành Phố HỒ CHÍ MINH', 'Active', '2025-04-01 20:13:45.937'),
(14, 'yuu2', '123', 'Qly', 'vonghia9a7@gmail.com', '0372858948', 'Tân Thới Nhì Hóc Môn Thành Phố HỒ CHÍ MINH', 'Active', '2025-04-01 20:37:44.517');

-- Chèn dữ liệu vào bảng Inventory_Transactions
INSERT INTO inventory_transactions (transaction_id, book_id, transaction_type, quantity, transaction_date, price_at_transaction, user_id) VALUES
(1, 1, 'Nhập', 10, '2025-03-31 10:00:00', 40000.00, 1),
(2, 2, 'Nhập', 10, '2025-03-31 10:00:00', 50000.00, 1),
(3, 3, 'Nhập', 10, '2025-03-31 10:00:00', 35000.00, 1),
(4, 4, 'Nhập', 10, '2025-03-31 10:00:00', 45000.00, 1),
(5, 5, 'Nhập', 10, '2025-03-31 10:00:00', 60000.00, 1),
(6, 6, 'Nhập', 10, '2025-03-31 10:00:00', 100000.00, 1),
(7, 7, 'Nhập', 10, '2025-03-31 10:00:00', 110000.00, 1),
(8, 8, 'Nhập', 10, '2025-03-31 10:00:00', 90000.00, 1),
(9, 9, 'Nhập', 10, '2025-03-31 10:00:00', 120000.00, 1),
(10, 10, 'Nhập', 10, '2025-03-31 10:00:00', 130000.00, 1),
(11, 11, 'Nhập', 10, '2025-03-31 10:00:00', 25000.00, 1),
(12, 12, 'Nhập', 10, '2025-03-31 10:00:00', 30000.00, 1),
(13, 13, 'Nhập', 10, '2025-03-31 10:00:00', 27000.00, 1),
(14, 14, 'Nhập', 10, '2025-03-31 10:00:00', 20000.00, 1),
(15, 15, 'Nhập', 10, '2025-03-31 10:00:00', 23000.00, 1),
(16, 16, 'Nhập', 10, '2025-03-31 10:00:00', 150000.00, 1),
(17, 17, 'Nhập', 10, '2025-03-31 10:00:00', 140000.00, 1),
(18, 18, 'Nhập', 10, '2025-03-31 10:00:00', 150000.00, 1),
(19, 19, 'Nhập', 10, '2025-03-31 10:00:00', 130000.00, 1),
(20, 20, 'Nhập', 10, '2025-03-31 10:00:00', 120000.00, 1),
(21, 21, 'Nhập', 10, '2025-03-31 10:00:00', 180000.00, 1),
(22, 22, 'Nhập', 10, '2025-03-31 10:00:00', 170000.00, 1),
(23, 23, 'Nhập', 10, '2025-03-31 10:00:00', 130000.00, 1),
(24, 24, 'Nhập', 10, '2025-03-31 10:00:00', 140000.00, 1),
(25, 25, 'Nhập', 10, '2025-03-31 10:00:00', 160000.00, 1),
(26, 26, 'Nhập', 10, '2025-03-31 10:00:00', 30000.00, 1),
(27, 27, 'Nhập', 10, '2025-03-31 10:00:00', 25000.00, 1),
(28, 28, 'Nhập', 10, '2025-03-31 10:00:00', 35000.00, 1),
(29, 29, 'Nhập', 10, '2025-03-31 10:00:00', 40000.00, 1),
(30, 30, 'Nhập', 10, '2025-03-31 10:00:00', 38000.00, 1);

-- Chèn dữ liệu vào bảng Shopping_Cart
INSERT INTO shopping_cart (cart_id, user_id, book_id, quantity) VALUES
(1, 10, NULL, 0),
(2, 9, NULL, 0),
(3, 8, NULL, 0),
(4, 7, NULL, 0),
(5, 6, NULL, 0),
(6, 5, NULL, 0),
(7, 4, NULL, 0),
(8, 3, NULL, 0),
(9, 2, NULL, 0),
(10, 1, NULL, 0),
(11, 11, NULL, 0),
(12, 13, NULL, 0),
(13, 14, NULL, 0);