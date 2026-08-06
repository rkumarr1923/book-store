-- =============================================================================
-- Book Store — Demo Seed Data
-- =============================================================================
-- Loaded by Spring's DataSourceInitializer after Hibernate creates the schema.
-- Profile: dev (ddl-auto = create-drop)
-- Passwords are BCrypt-hashed:
--   Admin    -> Admin@123!
--   Customer -> Customer@123!
-- =============================================================================

-- ─── Genres ──────────────────────────────────────────────────────────────────

INSERT INTO genres (name, slug, created_at) VALUES
  ('Romance',                   'romance',               NOW()),
  ('Mystery',                   'mystery',               NOW()),
  ('Science Fiction',           'science-fiction',       NOW()),
  ('Fantasy',                   'fantasy',               NOW()),
  ('Historical',                'historical',            NOW()),
  ('Biography',                 'biography',             NOW()),
  ('Self-help',                 'self-help',             NOW()),
  ('Memoir',                    'memoir',                NOW()),
  ('Travel',                    'travel',                NOW()),
  ('Cooking',                   'cooking',               NOW()),
  ('Children''s',               'childrens',             NOW()),
  ('Young Adult',               'young-adult',           NOW()),
  ('Comics & Graphic Novels',   'comics-graphic-novels', NOW()),
  ('Poetry',                    'poetry',                NOW()),
  ('Drama',                     'drama',                 NOW()),
  ('Science',                   'science',               NOW()),
  ('Philosophy',                'philosophy',            NOW()),
  ('Religion',                  'religion',              NOW()),
  ('Language Learning',         'language-learning',     NOW()),
  ('Non-fiction',               'non-fiction',           NOW())
ON CONFLICT (slug) DO NOTHING;

-- ─── Publishers ──────────────────────────────────────────────────────────────

INSERT INTO publishers (name, website, created_at) VALUES
  ('ABC Publishers',       'https://abcpublishers.com',       NOW()),
  ('Penguin Random House', 'https://penguinrandomhouse.com',  NOW()),
  ('HarperCollins India',  'https://harpercollins.co.in',     NOW()),
  ('Westland Books',       'https://westlandbooks.in',        NOW()),
  ('Simon & Schuster',     'https://simonandschuster.com',    NOW())
ON CONFLICT (name) DO NOTHING;

-- ─── Authors ─────────────────────────────────────────────────────────────────

INSERT INTO authors (name, biography, profile_image_url, created_at) VALUES
  ('Daniel Reed',
   'Daniel Reed is a writer, minimalist, and productivity coach based in San Francisco. With a passion for intentional living, Daniel has dedicated his career to helping individuals simplify their lives, one habit, one space, and one thought at a time. He is the author of The Joy of Minimalism, an acclaimed guide to decluttering both physically and mentally.',
   'https://randomuser.me/api/portraits/men/32.jpg', NOW()),

  ('Arjun Patel',
   'Arjun Patel is a bestselling author and motivational speaker known for his practical approach to personal development. His books on focus and productivity have transformed thousands of lives across India and globally.',
   'https://randomuser.me/api/portraits/men/54.jpg', NOW()),

  ('Raj Patel',
   'Raj Patel is an award-winning author and academic whose work explores the intersection of food, politics, and culture. He is a professor at the University of Texas and a fellow at the Food First Institute.',
   'https://randomuser.me/api/portraits/men/64.jpg', NOW()),

  ('James Wright',
   'James Wright is a life coach and author specialising in success psychology and goal achievement. His straightforward, actionable advice has earned him a loyal readership worldwide.',
   'https://randomuser.me/api/portraits/men/76.jpg', NOW()),

  ('James Adams',
   'James Adams is a novelist known for dark, atmospheric thrillers set in the English countryside. His compelling narratives and morally complex characters have made him a favourite on bestseller lists.',
   'https://randomuser.me/api/portraits/men/88.jpg', NOW()),

  ('Jessica Martin',
   'Jessica Martin writes deeply emotional literary fiction exploring love, loss, and the human condition. Her lyrical prose and multi-layered characters have garnered critical acclaim and a devoted readership.',
   'https://randomuser.me/api/portraits/women/44.jpg', NOW()),

  ('Laura Mitchell',
   'Laura Mitchell is a science fiction author whose speculative worlds challenge readers to examine the relationship between humanity and technology. She holds a PhD in Astrophysics and brings scientific rigour to her fiction.',
   'https://randomuser.me/api/portraits/women/58.jpg', NOW()),

  ('Clara Nelson',
   'Clara Nelson is a mystery writer celebrated for her tightly plotted psychological thrillers. A former journalist, Clara brings an investigative eye to every page of her fiction.',
   'https://randomuser.me/api/portraits/women/22.jpg', NOW()),

  ('Emily Parker',
   'Emily Parker is the author of several beloved children''s novels and young adult stories. Her warm, imaginative worlds and relatable protagonists have made her a household name among young readers.',
   'https://randomuser.me/api/portraits/women/36.jpg', NOW())
ON CONFLICT (name) DO NOTHING;

-- ─── Books ───────────────────────────────────────────────────────────────────

INSERT INTO books (title, description, cover_image_url, isbn, language, format, price, copies_sold, published_date, is_active, created_at, updated_at, publisher_id, author_id) VALUES
  ('The Joy of Minimalism',
   'Declutter your life to uncover peace, clarity, and joy. In The Joy of Minimalism, Daniel Reed guides you through practical strategies to declutter your mind, space, and schedule. Whether you are overwhelmed, overcommitted, or just over it, this book offers a calm, mindful approach to building a simpler, more fulfilling life.',
   'https://covers.openlibrary.org/b/id/8739161-L.jpg', '978-0-001-00001-1', 'English', 'PAPERBACK', 149.00, 1450, '2022-03-15', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='ABC Publishers'),
   (SELECT id FROM authors WHERE name='Daniel Reed')),

  ('The Art of Focus',
   'Practical guide to mastering focus and boosting productivity every day. Learn evidence-based techniques to eliminate distractions, sharpen your attention, and accomplish your most important work with less effort.',
   'https://covers.openlibrary.org/b/id/10909258-L.jpg', '978-0-001-00002-8', 'English', 'PAPERBACK', 399.00, 2100, '2021-06-10', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Penguin Random House'),
   (SELECT id FROM authors WHERE name='Arjun Patel')),

  ('The Art of Learning',
   'Master the mindset and methods for effective lifelong learning. Raj Patel synthesises cutting-edge research in cognitive science and distils it into a powerful framework that anyone can apply.',
   'https://covers.openlibrary.org/b/id/10792137-L.jpg', '978-0-001-00003-5', 'English', 'PAPERBACK', 259.00, 980, '2023-01-20', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Penguin Random House'),
   (SELECT id FROM authors WHERE name='Raj Patel')),

  ('The Path to Success',
   'A practical guide to achieving goals with clarity and confidence. James Wright shares the frameworks and mindsets used by top achievers across business, sport, and the arts.',
   'https://covers.openlibrary.org/b/id/8739441-L.jpg', '978-0-001-00004-2', 'English', 'PAPERBACK', 359.00, 1800, '2020-09-05', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='HarperCollins India'),
   (SELECT id FROM authors WHERE name='James Wright')),

  ('The Midnight Hour',
   'Haunting tale of a man''s journey through the shadows of a forgotten past. James Adams delivers another masterclass in psychological suspense as his protagonist races to uncover a truth buried for three decades.',
   'https://covers.openlibrary.org/b/id/10724697-L.jpg', '978-0-001-00005-9', 'English', 'PAPERBACK', 299.00, 3400, '2023-07-12', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Simon & Schuster'),
   (SELECT id FROM authors WHERE name='James Adams')),

  ('Beneath the Stars',
   'A heartwarming tale where two souls discover who they need. Jessica Martin weaves an unforgettable love story set against the rugged coastline of Kerala, exploring how unexpected connections can transform lives.',
   'https://covers.openlibrary.org/b/id/10796018-L.jpg', '978-0-001-00006-6', 'English', 'HARDCOVER', 499.00, 2700, '2022-11-28', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Westland Books'),
   (SELECT id FROM authors WHERE name='Jessica Martin')),

  ('The Final Frontier',
   'A mission to space with secrets to change humanity forever. Laura Mitchell''s genre-defining novel follows a team of astronauts who discover evidence of extraterrestrial civilisation and must decide whether to share it with the world.',
   'https://covers.openlibrary.org/b/id/9781682-L.jpg', '978-0-001-00007-3', 'English', 'PAPERBACK', 359.00, 1560, '2023-04-18', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Simon & Schuster'),
   (SELECT id FROM authors WHERE name='Laura Mitchell')),

  ('The Vanishing House',
   'A chilling mystery unfolds within a house that disappears. Clara Nelson''s most gripping thriller yet follows detective Priya Kapoor as she investigates a series of disappearances linked to an impossibly vanishing building.',
   'https://covers.openlibrary.org/b/id/11117541-L.jpg', '978-0-001-00008-0', 'English', 'EBOOK', 99.00, 4100, '2024-01-09', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='ABC Publishers'),
   (SELECT id FROM authors WHERE name='Clara Nelson')),

  ('The Lost Kitten',
   'A heartwarming tale of courage, friendship, and feline adventure. Emily Parker''s charming story follows nine-year-old Ananya as she embarks on a neighbourhood-wide search for her beloved cat, discovering unexpected friendships along the way.',
   'https://covers.openlibrary.org/b/id/10793482-L.jpg', '978-0-001-00009-7', 'English', 'HARDCOVER', 339.00, 890, '2023-10-03', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Westland Books'),
   (SELECT id FROM authors WHERE name='Emily Parker')),

  ('Deep Work',
   'Rules for focused success in a distracted world. A compelling argument for the value of intense, uninterrupted concentration, with practical strategies for restructuring your work habits.',
   'https://covers.openlibrary.org/b/id/8096302-L.jpg', '978-0-001-00010-3', 'English', 'PAPERBACK', 449.00, 5200, '2020-05-20', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='HarperCollins India'),
   (SELECT id FROM authors WHERE name='Arjun Patel')),

  ('Stars and Shadows',
   'An epic fantasy saga of magic, betrayal, and redemption. Set in a world where stars are gods and shadows are weapons, this sweeping debut has already been compared to classics of the genre.',
   'https://covers.openlibrary.org/b/id/10710168-L.jpg', '978-0-001-00011-0', 'English', 'PAPERBACK', 549.00, 1320, '2024-03-22', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Simon & Schuster'),
   (SELECT id FROM authors WHERE name='Laura Mitchell')),

  ('Echoes of Love',
   'When two strangers meet on a monsoon night, their lives are changed forever. A tender, atmospheric romance set in contemporary Mumbai that explores love, identity, and the ties that bind us.',
   'https://covers.openlibrary.org/b/id/10795219-L.jpg', '978-0-001-00012-7', 'English', 'PAPERBACK', 279.00, 2200, '2022-08-14', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Westland Books'),
   (SELECT id FROM authors WHERE name='Jessica Martin')),

  ('The Silent Witness',
   'A courtroom thriller where the only evidence is a memory that cannot be trusted. Clara Nelson returns with a taut legal thriller that keeps readers guessing until the final verdict.',
   'https://covers.openlibrary.org/b/id/8318137-L.jpg', '978-0-001-00013-4', 'English', 'HARDCOVER', 529.00, 1670, '2023-09-05', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Penguin Random House'),
   (SELECT id FROM authors WHERE name='Clara Nelson')),

  ('Atomic Habits for Students',
   'A student''s guide to building habits that stick and goals that matter. Adapted from the principles of habit science, this practical workbook gives students the tools to transform their academic and personal lives.',
   'https://covers.openlibrary.org/b/id/10793578-L.jpg', '978-0-001-00014-1', 'English', 'PAPERBACK', 199.00, 3800, '2021-07-30', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='HarperCollins India'),
   (SELECT id FROM authors WHERE name='Arjun Patel')),

  ('The Code of Stars',
   'When an AI achieves consciousness, it must choose between its creators and the truth. Laura Mitchell''s thought-provoking science fiction explores the ethical boundaries of artificial intelligence in a world not so different from our own.',
   'https://covers.openlibrary.org/b/id/8096301-L.jpg', '978-0-001-00015-8', 'English', 'EBOOK', 179.00, 2450, '2024-05-16', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Simon & Schuster'),
   (SELECT id FROM authors WHERE name='Laura Mitchell'));

-- ─── Book-Genre Associations ──────────────────────────────────────────────────

INSERT INTO book_genres (book_id, genre_id)
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Joy of Minimalism'      AND g.slug IN ('self-help','non-fiction')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Art of Focus'           AND g.slug IN ('self-help','non-fiction')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Art of Learning'        AND g.slug IN ('self-help','non-fiction')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Path to Success'        AND g.slug IN ('self-help','non-fiction')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Midnight Hour'          AND g.slug IN ('mystery','drama')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'Beneath the Stars'          AND g.slug IN ('romance','drama')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Final Frontier'         AND g.slug IN ('science-fiction')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Vanishing House'        AND g.slug IN ('mystery')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Lost Kitten'            AND g.slug IN ('childrens')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'Deep Work'                  AND g.slug IN ('self-help','non-fiction')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'Stars and Shadows'          AND g.slug IN ('fantasy')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'Echoes of Love'             AND g.slug IN ('romance')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Silent Witness'         AND g.slug IN ('mystery','drama')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'Atomic Habits for Students' AND g.slug IN ('self-help','young-adult')
UNION ALL
SELECT b.id, g.id FROM books b, genres g WHERE b.title = 'The Code of Stars'          AND g.slug IN ('science-fiction');

-- ─── Users ───────────────────────────────────────────────────────────────────
-- Passwords:
--   admin@bookworm.com    -> Admin@123!
--   customer@bookworm.com -> Customer@123!

-- ─── Addresses ───────────────────────────────────────────────────────────────

INSERT INTO addresses (user_id, first_name, last_name, address_line1, address_line2, city, state, country, pin_code, phone_number, email, is_default, created_at)
SELECT
  u.id, 'Ananya', 'Sharma',
  '42 Indiranagar, 100 Feet Road', 'Flat 3B',
  'Bengaluru', 'Karnataka', 'India', '560038',
  '+919876543210', 'customer@bookworm.com', true, NOW()
FROM users u WHERE u.email = 'customer@bookworm.com';

-- ─── Coupons ─────────────────────────────────────────────────────────────────

INSERT INTO coupons (code, discount_type, discount_value, min_order_value, expires_at, is_active, created_at) VALUES
  ('SAVE100',   'FIXED',       100.00,   500.00, NOW() + INTERVAL '1 year',   true, NOW()),
  ('WELCOME20', 'PERCENTAGE',   20.00,   200.00, NOW() + INTERVAL '1 year',   true, NOW()),
  ('BOOKFEST',  'FIXED',       200.00,   800.00, NOW() + INTERVAL '6 months', true, NOW()),
  ('SUMMER50',  'FIXED',        50.00,     0.00, NOW() + INTERVAL '3 months', true, NOW())
ON CONFLICT (code) DO NOTHING;

-- ─── Wishlist ─────────────────────────────────────────────────────────────────

INSERT INTO wishlists (user_id, created_at)
SELECT id, NOW() FROM users WHERE email = 'customer@bookworm.com'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO wishlist_items (wishlist_id, book_id, created_at)
SELECT w.id, b.id, NOW()
FROM wishlists w
JOIN users u ON u.id = w.user_id
CROSS JOIN books b
WHERE u.email = 'customer@bookworm.com'
  AND b.title IN ('The Midnight Hour', 'The Final Frontier', 'Stars and Shadows')
ON CONFLICT (wishlist_id, book_id) DO NOTHING;

-- ─── Cart ─────────────────────────────────────────────────────────────────────

INSERT INTO carts (user_id, created_at, updated_at)
SELECT id, NOW(), NOW() FROM users WHERE email = 'customer@bookworm.com'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO cart_items (cart_id, book_id, quantity, created_at)
SELECT c.id, b.id, 1, NOW()
FROM carts c
JOIN users u ON u.id = c.user_id
CROSS JOIN books b
WHERE u.email = 'customer@bookworm.com'
  AND b.title IN ('The Joy of Minimalism', 'The Path to Success')
ON CONFLICT (cart_id, book_id) DO NOTHING;

-- ─── Reviews ─────────────────────────────────────────────────────────────────

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 5, 'Absolutely life-changing! Recommended to all my friends.', NOW() - INTERVAL '10 days'
FROM users u, books b
WHERE u.email = 'customer@bookworm.com' AND b.title = 'The Joy of Minimalism'
ON CONFLICT (book_id, user_id) DO NOTHING;

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 4, 'Very practical and well-written. Easy to apply.', NOW() - INTERVAL '5 days'
FROM users u, books b
WHERE u.email = 'customer@bookworm.com' AND b.title = 'The Art of Focus'
ON CONFLICT (book_id, user_id) DO NOTHING;

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 5, 'A must-read for any aspiring entrepreneur!', NOW() - INTERVAL '3 days'
FROM users u, books b
WHERE u.email = 'customer@bookworm.com' AND b.title = 'The Path to Success'
ON CONFLICT (book_id, user_id) DO NOTHING;

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 5, 'Best thriller I have read in years. Could not put it down.', NOW() - INTERVAL '15 days'
FROM users u, books b
WHERE u.email = 'admin@bookworm.com' AND b.title = 'The Midnight Hour'
ON CONFLICT (book_id, user_id) DO NOTHING;

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 4, 'Beautiful writing, stays with you long after the last page.', NOW() - INTERVAL '8 days'
FROM users u, books b
WHERE u.email = 'admin@bookworm.com' AND b.title = 'Beneath the Stars'
ON CONFLICT (book_id, user_id) DO NOTHING;

-- ─── Orders ──────────────────────────────────────────────────────────────────

-- Past confirmed order (customer) — DELIVERED
INSERT INTO orders (user_id, order_number, status, subtotal, tax_amount, delivery_charge, discount_amount, total_amount, coupon_id, delivery_address_snapshot, placed_at, estimated_delivery_date, created_at, updated_at)
SELECT
  u.id,
  'BST-20250701-00001',
  'DELIVERED',
  508.00, 61.00, 0.00, 100.00, 469.00,
  (SELECT id FROM coupons WHERE code = 'SAVE100'),
  '{"firstName":"Ananya","lastName":"Sharma","addressLine1":"42 Indiranagar, 100 Feet Road","addressLine2":"Flat 3B","city":"Bengaluru","state":"Karnataka","country":"India","pinCode":"560038","phoneNumber":"+919876543210","email":"customer@bookworm.com"}'::jsonb,
  NOW() - INTERVAL '20 days',
  (CURRENT_DATE - INTERVAL '13 days')::date,
  NOW() - INTERVAL '20 days',
  NOW() - INTERVAL '20 days'
FROM users u WHERE u.email = 'customer@bookworm.com'
ON CONFLICT (order_number) DO NOTHING;

-- Order items for first order
INSERT INTO order_items (order_id, book_id, title, cover_image_url, format, unit_price, quantity, line_total, created_at)
SELECT
  o.id, b.id, b.title, b.cover_image_url, b.format, b.price, 1, b.price, NOW() - INTERVAL '20 days'
FROM orders o
CROSS JOIN books b
WHERE o.order_number = 'BST-20250701-00001'
  AND b.title IN ('The Joy of Minimalism', 'The Path to Success');

-- Payment for first order
INSERT INTO payments (order_id, payment_method, status, payable_amount, paid_at, created_at)
SELECT o.id, 'CREDIT_CARD', 'SUCCESS', 469.00, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'
FROM orders o WHERE o.order_number = 'BST-20250701-00001'
ON CONFLICT (order_id) DO NOTHING;

-- Second order — CONFIRMED, awaiting delivery
INSERT INTO orders (user_id, order_number, status, subtotal, tax_amount, delivery_charge, discount_amount, total_amount, coupon_id, delivery_address_snapshot, placed_at, estimated_delivery_date, created_at, updated_at)
SELECT
  u.id,
  'BST-20250715-00002',
  'CONFIRMED',
  449.00, 54.00, 0.00, 0.00, 503.00,
  NULL,
  '{"firstName":"Ananya","lastName":"Sharma","addressLine1":"42 Indiranagar, 100 Feet Road","addressLine2":"Flat 3B","city":"Bengaluru","state":"Karnataka","country":"India","pinCode":"560038","phoneNumber":"+919876543210","email":"customer@bookworm.com"}'::jsonb,
  NOW() - INTERVAL '6 days',
  (CURRENT_DATE + INTERVAL '1 day')::date,
  NOW() - INTERVAL '6 days',
  NOW() - INTERVAL '6 days'
FROM users u WHERE u.email = 'customer@bookworm.com'
ON CONFLICT (order_number) DO NOTHING;

INSERT INTO order_items (order_id, book_id, title, cover_image_url, format, unit_price, quantity, line_total, created_at)
SELECT
  o.id, b.id, b.title, b.cover_image_url, b.format, b.price, 1, b.price, NOW() - INTERVAL '6 days'
FROM orders o
CROSS JOIN books b
WHERE o.order_number = 'BST-20250715-00002'
  AND b.title = 'Deep Work';

INSERT INTO payments (order_id, payment_method, status, payable_amount, paid_at, created_at)
SELECT o.id, 'UPI', 'SUCCESS', 503.00, NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'
FROM orders o WHERE o.order_number = 'BST-20250715-00002'
ON CONFLICT (order_id) DO NOTHING;

-- ─── User Followed Authors ────────────────────────────────────────────────────

INSERT INTO user_followed_authors (user_id, author_id, followed_at)
SELECT u.id, a.id, NOW() - INTERVAL '30 days'
FROM users u
CROSS JOIN authors a
WHERE u.email = 'customer@bookworm.com'
  AND a.name IN ('Daniel Reed', 'Laura Mitchell')
ON CONFLICT (user_id, author_id) DO NOTHING;
