USE sharenest_db;

-- The application seeds equivalent data automatically when app.seed-data=true.
-- Password for all users below is "password".
-- For easiest setup, prefer the built-in DataInitializer because it generates fresh BCrypt hashes.

INSERT INTO categories (name, description) VALUES
('Tools', 'Drills, ladders, repair kits and home tools'),
('Kitchen', 'Appliances and cookware for occasional use'),
('Books', 'Books, guides and learning material'),
('Outdoor', 'Sports, garden and travel equipment');

INSERT INTO users (full_name, email, password, phone, address, role, enabled, created_at, updated_at) VALUES
('Admin User', 'admin@sharenest.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFYLWJQ7lwpT6o1DBs.KWr.', '9876543210', 'Community Office', 'ADMIN', 1, NOW(6), NOW(6)),
('Priya Sharma', 'priya@sharenest.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFYLWJQ7lwpT6o1DBs.KWr.', '9876500001', 'Block A, Flat 102', 'USER', 1, NOW(6), NOW(6)),
('Arjun Mehta', 'arjun@sharenest.local', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFYLWJQ7lwpT6o1DBs.KWr.', '9876500002', 'Block B, Flat 305', 'USER', 1, NOW(6), NOW(6));

INSERT INTO items (title, description, location, image_url, status, category_id, owner_id, created_at, updated_at) VALUES
('Cordless Drill Kit', '18V drill with bits, charger and safety case.', 'Block A Lobby', 'https://images.unsplash.com/photo-1504148455328-c376907d081c?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 1, 2, NOW(6), NOW(6)),
('Large Pressure Cooker', 'Eight litre pressure cooker for gatherings and festivals.', 'Block B Security Desk', 'https://images.unsplash.com/photo-1556911220-bff31c812dba?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 2, 3, NOW(6), NOW(6)),
('Weekend Camping Tent', 'Four-person waterproof tent with pegs and carry bag.', 'Clubhouse Store', 'https://images.unsplash.com/photo-1504851149312-7a075b496cc7?auto=format&fit=crop&w=900&q=80', 'AVAILABLE', 4, 2, NOW(6), NOW(6));
