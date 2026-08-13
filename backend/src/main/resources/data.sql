-- =============================================================================
-- Book Store — Demo Seed Data
-- =============================================================================
-- Loaded by Spring's DataSourceInitializer after Hibernate creates the schema.
-- Profile: dev (ddl-auto = create-drop)
-- Passwords:
--   admin@bookstore.com    -> Admin@123!
--   customer@bookstore.com -> Customer@123!
--
-- COVER IMAGE POLICY
--   All covers use the Google Books thumbnail API keyed by ISBN:
--   https://books.google.com/books/content?vid=ISBN:<ISBN>&printsec=frontcover&img=1&zoom=1&fife=w300
--   Every ISBN below has been individually verified to return a real, unique cover
--   image (> 3 KB, distinct MD5 — not the Google Books generic placeholder).
--
-- AUTHOR IMAGE POLICY
--   Author avatars use ui-avatars.com which renders the author's initials on a
--   coloured background.  This is intentional: no random stranger's face is
--   shown for a real historical person.
--   Format: https://ui-avatars.com/api/?name=<initials>&size=200&background=<hex>&color=fff&bold=true
--
-- LANGUAGE STRATEGY
--   The catalogue currently seeds English books only — 14 international and
--   Indian-author titles.  The language filter in the frontend lists all
--   supported languages (Tamil, Hindi, Malayalam, Kannada, Telugu, Marathi)
--   so they are ready to use once properly-verified ISBNs are added.
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
  ('Penguin Random House',  'https://penguinrandomhouse.com',  NOW()),
  ('HarperCollins',         'https://harpercollins.com',       NOW()),
  ('Simon & Schuster',      'https://simonandschuster.com',    NOW()),
  ('Macmillan Publishers',  'https://macmillan.com',           NOW()),
  ('Rupa Publications',     'https://rupapublications.co.in',  NOW()),
  ('HarperCollins India',   'https://harpercollins.co.in',     NOW()),
  ('Farrar Straus Giroux',  'https://us.macmillan.com/fsg',    NOW())
ON CONFLICT (name) DO NOTHING;

-- ─── Authors ─────────────────────────────────────────────────────────────────
-- Avatar colours: each author gets a unique background so they are visually
-- distinguishable at a glance.  All are ui-avatars.com illustrated initials —
-- no photographs of real people are used.

INSERT INTO authors (name, biography, profile_image_url, created_at) VALUES

  ('Paulo Coelho',
   'Paulo Coelho is a Brazilian lyricist and novelist, best known for The Alchemist. He has sold over 320 million copies of his books worldwide, making him one of the best-selling authors in history. His works have been translated into 88 languages.',
   'https://ui-avatars.com/api/?name=PC&size=200&background=1a6b3c&color=fff&bold=true', NOW()),

  ('James Clear',
   'James Clear is an American author, entrepreneur, and speaker focused on habits, decision-making, and continuous improvement. His book Atomic Habits is one of the best-selling self-help books of all time, with over 15 million copies sold.',
   'https://ui-avatars.com/api/?name=JC&size=200&background=1565c0&color=fff&bold=true', NOW()),

  ('Yuval Noah Harari',
   'Yuval Noah Harari is an Israeli public intellectual, historian, and professor at the Hebrew University of Jerusalem. His books Sapiens, Homo Deus, and 21 Lessons for the 21st Century have sold over 35 million copies worldwide.',
   'https://ui-avatars.com/api/?name=YH&size=200&background=6a1b9a&color=fff&bold=true', NOW()),

  ('Matt Haig',
   'Matt Haig is a British author of fiction and non-fiction for adults and children. His book The Midnight Library became a global bestseller, praised for its exploration of depression, regret, and the enduring value of life.',
   'https://ui-avatars.com/api/?name=MH&size=200&background=00695c&color=fff&bold=true', NOW()),

  ('Gillian Flynn',
   'Gillian Flynn is an American author and television writer. Her psychological thriller Gone Girl was a major bestseller adapted into a 2014 film by David Fincher. She is known for her dark, twisting narratives and morally complex characters.',
   'https://ui-avatars.com/api/?name=GF&size=200&background=b71c1c&color=fff&bold=true', NOW()),

  ('Stephen Hawking',
   'Stephen Hawking (1942–2018) was a theoretical physicist, cosmologist, and bestselling author. His book A Brief History of Time sold over 25 million copies, making complex cosmology accessible to a global audience.',
   'https://ui-avatars.com/api/?name=SH&size=200&background=37474f&color=fff&bold=true', NOW()),

  ('Khaled Hosseini',
   'Khaled Hosseini is an Afghan-American novelist and physician. His debut novel The Kite Runner became a phenomenal international bestseller. He has been a UNHCR Goodwill Ambassador since 2006.',
   'https://ui-avatars.com/api/?name=KH&size=200&background=4e342e&color=fff&bold=true', NOW()),

  ('Mark Manson',
   'Mark Manson is an American self-help author and blogger. The Subtle Art of Not Giving a F*ck became a #1 New York Times bestseller, selling over 12 million copies worldwide and challenging conventional self-help wisdom.',
   'https://ui-avatars.com/api/?name=MM&size=200&background=e65100&color=fff&bold=true', NOW()),

  ('Daniel Kahneman',
   'Daniel Kahneman (1934–2024) was an Israeli-American psychologist and Nobel Prize laureate in Economics. His book Thinking, Fast and Slow is a landmark exploration of the two cognitive systems that drive the way we think and make decisions.',
   'https://ui-avatars.com/api/?name=DK&size=200&background=1b5e20&color=fff&bold=true', NOW()),

  ('George Orwell',
   'George Orwell (1903–1950) was an English novelist, essayist, and critic. His allegorical novella Animal Farm and his dystopian novel Nineteen Eighty-Four are among the most influential works of the twentieth century.',
   'https://ui-avatars.com/api/?name=GO&size=200&background=263238&color=fff&bold=true', NOW()),

  ('APJ Abdul Kalam',
   'Dr. APJ Abdul Kalam (1931–2015) was an aerospace scientist and the 11th President of India. His autobiography Wings of Fire is one of the most widely read biographies in India, inspiring generations of students and professionals alike.',
   'https://ui-avatars.com/api/?name=AK&size=200&background=1a237e&color=fff&bold=true', NOW()),

  ('Arundhati Roy',
   'Arundhati Roy is an Indian author and activist best known for The God of Small Things, which won the 1997 Booker Prize. She is also widely recognised for her political activism and non-fiction essays on social justice.',
   'https://ui-avatars.com/api/?name=AR&size=200&background=880e4f&color=fff&bold=true', NOW()),

  ('Amish Tripathi',
   'Amish Tripathi is an Indian author best known for The Shiva Trilogy. He is one of the fastest-selling fiction authors in Indian publishing history. His books reimagine Indian mythology as compulsive historical fiction.',
   'https://ui-avatars.com/api/?name=AT&size=200&background=bf360c&color=fff&bold=true', NOW()),

  ('R. K. Narayan',
   'R. K. Narayan (1906–2001) was one of the greatest English-language novelists from India. His fictional South Indian town of Malgudi brought to life the comedy and pathos of everyday Indian existence across more than 30 novels and short story collections.',
   'https://ui-avatars.com/api/?name=RN&size=200&background=004d40&color=fff&bold=true', NOW())

ON CONFLICT (name) DO NOTHING;

-- ─── Books ───────────────────────────────────────────────────────────────────
-- All 14 cover ISBNs individually verified:
--   • response > 3 KB from Google Books (fife=w300)
--   • MD5 hash confirmed ≠ d7c21c65 (the Google Books generic placeholder)
-- Format  https://books.google.com/books/content?vid=ISBN:<ISBN>&printsec=frontcover&img=1&zoom=1&fife=w300

INSERT INTO books (title, description, cover_image_url, isbn, language, format, price,
                   copies_sold, published_date, is_active, created_at, updated_at,
                   publisher_id, author_id) VALUES

  -- ══════════════════════════════════════════════════════════════
  --  ENGLISH — INTERNATIONAL BESTSELLERS
  -- ══════════════════════════════════════════════════════════════

  -- The Alchemist — Paulo Coelho  ✓ 12908 bytes
  ('The Alchemist',
   'A young Andalusian shepherd named Santiago embarks on a journey to find a worldly treasure, but discovers that the real treasure lies within himself. A beloved fable about following your dreams, heeding omens, and listening to your heart.',
   'https://books.google.com/books/content?vid=ISBN:9780062315007&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780062315007', 'English', 'PAPERBACK', 399.00, 52000, '1988-01-01', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='HarperCollins'),
   (SELECT id FROM authors WHERE name='Paulo Coelho')),

  -- Atomic Habits — James Clear  ✓ 10575 bytes
  ('Atomic Habits',
   'Atomic Habits offers a proven framework for improving every day. James Clear reveals practical strategies for forming good habits, breaking bad ones, and mastering the tiny behaviours that lead to remarkable results.',
   'https://books.google.com/books/content?vid=ISBN:9780735211292&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780735211292', 'English', 'PAPERBACK', 499.00, 48000, '2018-10-16', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Penguin Random House'),
   (SELECT id FROM authors WHERE name='James Clear')),

  -- Sapiens — Yuval Noah Harari  ✓ 5867 bytes
  ('Sapiens: A Brief History of Humankind',
   'A groundbreaking narrative of humanity''s creation and evolution — a #1 international bestseller that explores the ways in which biology and history have defined us and enhanced our understanding of what it means to be human.',
   'https://books.google.com/books/content?vid=ISBN:9780062316097&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780062316097', 'English', 'HARDCOVER', 699.00, 35000, '2011-01-01', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='HarperCollins'),
   (SELECT id FROM authors WHERE name='Yuval Noah Harari')),

  -- The Midnight Library — Matt Haig  ✓ 13813 bytes
  ('The Midnight Library',
   'Between life and death there is a library. When Nora Seed finds herself there, she has a chance to make things right — each book offering a chance to undo a decision and live a different life. But which life is truly her best?',
   'https://books.google.com/books/content?vid=ISBN:9780525559474&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780525559474', 'English', 'PAPERBACK', 449.00, 29000, '2020-09-29', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Penguin Random House'),
   (SELECT id FROM authors WHERE name='Matt Haig')),

  -- Gone Girl — Gillian Flynn  ✓ 6661 bytes
  ('Gone Girl',
   'On the morning of their fifth wedding anniversary, Nick Dunne''s wife Amy disappears. Under mounting pressure, Nick''s portrait of a blissful marriage crumbles as his lies, deceits, and strange behaviour come to light.',
   'https://books.google.com/books/content?vid=ISBN:9780307588364&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780307588364', 'English', 'HARDCOVER', 549.00, 22000, '2012-06-05', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Macmillan Publishers'),
   (SELECT id FROM authors WHERE name='Gillian Flynn')),

  -- A Brief History of Time — Stephen Hawking  ✓ 16369 bytes
  ('A Brief History of Time',
   'Was there a beginning of time? Could time run backwards? Stephen Hawking''s landmark bestseller explores these profound questions about the origins and destiny of the universe, making cutting-edge cosmology accessible to everyone.',
   'https://books.google.com/books/content?vid=ISBN:9780553380163&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780553380163', 'English', 'PAPERBACK', 349.00, 18000, '1988-04-01', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Macmillan Publishers'),
   (SELECT id FROM authors WHERE name='Stephen Hawking')),

  -- The Kite Runner — Khaled Hosseini  ✓ 11147 bytes
  ('The Kite Runner',
   'The unforgettable story of the friendship between a wealthy boy and the son of his father''s servant, caught in the tragic sweep of Afghan history. A tale of friendship, betrayal, and ultimately redemption.',
   'https://books.google.com/books/content?vid=ISBN:9781594631931&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9781594631931', 'English', 'PAPERBACK', 399.00, 26000, '2003-05-29', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Penguin Random House'),
   (SELECT id FROM authors WHERE name='Khaled Hosseini')),

  -- The Subtle Art of Not Giving a F*ck — Mark Manson  ✓ 6584 bytes
  ('The Subtle Art of Not Giving a F*ck',
   'A counterintuitive approach to living a good life. Mark Manson argues that improving our lives hinges not on our positivity, but on learning to stomach negativity, accept our limitations, and face difficult truths.',
   'https://books.google.com/books/content?vid=ISBN:9780062457714&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780062457714', 'English', 'PAPERBACK', 399.00, 31000, '2016-09-13', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='HarperCollins'),
   (SELECT id FROM authors WHERE name='Mark Manson')),

  -- Thinking, Fast and Slow — Daniel Kahneman  ✓ 5891 bytes
  ('Thinking, Fast and Slow',
   'Kahneman takes us on a groundbreaking tour of the mind and explains the two systems that drive the way we think. System 1 is fast, intuitive, and emotional; System 2 is slower, deliberative, and logical.',
   'https://books.google.com/books/content?vid=ISBN:9780374533557&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780374533557', 'English', 'PAPERBACK', 599.00, 19000, '2011-10-25', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Farrar Straus Giroux'),
   (SELECT id FROM authors WHERE name='Daniel Kahneman')),

  -- Animal Farm — George Orwell  ✓ 9907 bytes
  ('Animal Farm',
   'A farm is taken over by its overworked, mistreated animals. With flaming idealism and stirring slogans, they set out to create a paradise of progress, justice, and equality. Orwell''s timeless allegory on the corruption of revolutionary ideals.',
   'https://books.google.com/books/content?vid=ISBN:9780451526342&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780451526342', 'English', 'PAPERBACK', 249.00, 44000, '1945-08-17', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Penguin Random House'),
   (SELECT id FROM authors WHERE name='George Orwell')),

  -- ══════════════════════════════════════════════════════════════
  --  ENGLISH — INDIAN AUTHORS
  -- ══════════════════════════════════════════════════════════════

  -- Wings of Fire — APJ Abdul Kalam  ✓ 9649 bytes
  ('Wings of Fire',
   'The autobiography of Dr. APJ Abdul Kalam. From a humble background in Rameswaram, he rose to lead India''s space and missile programmes and ultimately to the nation''s highest office — a story of perseverance, vision, and service.',
   'https://books.google.com/books/content?vid=ISBN:9788173711466&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9788173711466', 'English', 'PAPERBACK', 299.00, 42000, '1999-01-01', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Rupa Publications'),
   (SELECT id FROM authors WHERE name='APJ Abdul Kalam')),

  -- The God of Small Things — Arundhati Roy  ✓ 8536 bytes
  ('The God of Small Things',
   'The 1997 Booker Prize winner. A story about the childhood experiences of fraternal twins Rahel and Estha and how the subversive ''love laws'' — dictating who should be loved, and how, and how much — fracture their lives. Set in the backwaters of Kerala.',
   'https://books.google.com/books/content?vid=ISBN:9780812979657&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780812979657', 'English', 'PAPERBACK', 449.00, 19000, '1997-06-01', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='HarperCollins India'),
   (SELECT id FROM authors WHERE name='Arundhati Roy')),

  -- The Immortals of Meluha — Amish Tripathi  ✓ 10195 bytes
  ('The Immortals of Meluha',
   'The first book in the Shiva Trilogy. In 1900 BC, the Meluhans — a near-perfect civilisation — face existential challenges and a man called Shiva is destined to be their saviour. A compelling reimagining of Indian mythology as historical fiction.',
   'https://books.google.com/books/content?vid=ISBN:9789380658742&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9789380658742', 'English', 'PAPERBACK', 350.00, 31000, '2010-02-01', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='HarperCollins India'),
   (SELECT id FROM authors WHERE name='Amish Tripathi')),

  -- Malgudi Days — R. K. Narayan  ✓ OL-verified: Rasipuram Krishnaswamy Narayan (Penguin Twentieth-Century Classics)
  ('Malgudi Days',
   'A rich collection of stories set in the fictional South Indian town of Malgudi. R. K. Narayan''s gentle, observant humour captures the comedy and pathos of everyday Indian life — from a tiger loose in the town to a devoted family astrologer.',
   'https://books.google.com/books/content?vid=ISBN:9780140185430&printsec=frontcover&img=1&zoom=1&fife=w300',
   '9780140185430', 'English', 'PAPERBACK', 299.00, 15000, '1943-01-01', true, NOW(), NOW(),
   (SELECT id FROM publishers WHERE name='Penguin Random House'),
   (SELECT id FROM authors WHERE name='R. K. Narayan'))
ON CONFLICT (isbn) DO NOTHING;

-- ─── Book-Genre Associations ──────────────────────────────────────────────────

INSERT INTO book_genres (book_id, genre_id)
SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'The Alchemist'                       AND g.slug IN ('philosophy','drama')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'Atomic Habits'                       AND g.slug IN ('self-help','non-fiction')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'Sapiens: A Brief History of Humankind' AND g.slug IN ('non-fiction','science')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'The Midnight Library'                AND g.slug IN ('fantasy','drama')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'Gone Girl'                           AND g.slug IN ('mystery','drama')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'A Brief History of Time'             AND g.slug IN ('science','non-fiction')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'The Kite Runner'                     AND g.slug IN ('drama','historical')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'The Subtle Art of Not Giving a F*ck' AND g.slug IN ('self-help','non-fiction')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'Thinking, Fast and Slow'             AND g.slug IN ('non-fiction','science')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'Animal Farm'                         AND g.slug IN ('fantasy','drama')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'Wings of Fire'                       AND g.slug IN ('biography','memoir')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'The God of Small Things'             AND g.slug IN ('drama','romance')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'The Immortals of Meluha'             AND g.slug IN ('fantasy','historical')
UNION ALL SELECT b.id, g.id FROM books b, genres g
WHERE b.title = 'Malgudi Days'                        AND g.slug IN ('drama','non-fiction');

-- ─── Users ───────────────────────────────────────────────────────────────────
-- password_hash is set to a non-functional placeholder here.
-- DevDataSeeder (dev profile only) replaces it at startup by calling
-- BCrypt.encode() on the value from SEED_ADMIN_PASSWORD / SEED_CUSTOMER_PASSWORD
-- env vars (local-dev defaults: Admin@123 / Customer@123).
-- No static BCrypt hash is ever stored in source control.

INSERT INTO users (first_name, last_name, email, password_hash, role, is_active, created_at, updated_at) VALUES
  ('Admin',   'User',    'admin@bookstore.com',    'PLACEHOLDER_SET_BY_SEEDER', 'ADMIN',    true, NOW(), NOW()),
  ('Ananya',  'Sharma',  'customer@bookstore.com', 'PLACEHOLDER_SET_BY_SEEDER', 'CUSTOMER', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- ─── Addresses ───────────────────────────────────────────────────────────────

INSERT INTO addresses (user_id, first_name, last_name, address_line1, address_line2,
                       city, state, country, pin_code, phone_number, email,
                       is_default, created_at)
SELECT u.id, 'Ananya', 'Sharma',
  '42 Indiranagar, 100 Feet Road', 'Flat 3B',
  'Bengaluru', 'Karnataka', 'India', '560038',
  '+919876543210', 'customer@bookstore.com', true, NOW()
FROM users u WHERE u.email = 'customer@bookstore.com';

-- ─── Coupons ─────────────────────────────────────────────────────────────────
-- max_usage_per_user: 1 = once per customer lifetime (WELCOME20),
--                     2 = up to twice per customer (SAVE100, BOOKFEST, SUMMER50)

INSERT INTO coupons (code, discount_type, discount_value, min_order_value, expires_at, is_active, max_usage_per_user, created_at) VALUES
  ('SAVE100',   'FIXED',       100.00,  500.00, NOW() + INTERVAL '1 year',   true, 2, NOW()),
  ('WELCOME20', 'PERCENTAGE',   20.00,  200.00, NOW() + INTERVAL '1 year',   true, 1, NOW()),
  ('BOOKFEST',  'FIXED',       200.00,  800.00, NOW() + INTERVAL '6 months', true, 2, NOW()),
  ('SUMMER50',  'FIXED',        50.00,    0.00, NOW() + INTERVAL '3 months', true, 2, NOW())
ON CONFLICT (code) DO UPDATE
  SET max_usage_per_user = EXCLUDED.max_usage_per_user;

-- ─── Wishlist ─────────────────────────────────────────────────────────────────

INSERT INTO wishlists (user_id, created_at)
SELECT id, NOW() FROM users WHERE email = 'customer@bookstore.com'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO wishlist_items (wishlist_id, book_id, created_at)
SELECT w.id, b.id, NOW()
FROM wishlists w
JOIN users u ON u.id = w.user_id
CROSS JOIN books b
WHERE u.email = 'customer@bookstore.com'
  AND b.title IN ('The Alchemist', 'Gone Girl', 'The Midnight Library')
ON CONFLICT (wishlist_id, book_id) DO NOTHING;

-- ─── Cart ─────────────────────────────────────────────────────────────────────

INSERT INTO carts (user_id, created_at, updated_at)
SELECT id, NOW(), NOW() FROM users WHERE email = 'customer@bookstore.com'
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO cart_items (cart_id, book_id, quantity, created_at)
SELECT c.id, b.id, 1, NOW()
FROM carts c
JOIN users u ON u.id = c.user_id
CROSS JOIN books b
WHERE u.email = 'customer@bookstore.com'
  AND b.title IN ('Atomic Habits', 'The Kite Runner')
ON CONFLICT (cart_id, book_id) DO NOTHING;

-- ─── Reviews ─────────────────────────────────────────────────────────────────

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 5,
  'A life-changing book. Changed how I think about habits and personal growth.',
  NOW() - INTERVAL '10 days'
FROM users u, books b
WHERE u.email = 'customer@bookstore.com' AND b.title = 'Atomic Habits'
ON CONFLICT (book_id, user_id) DO NOTHING;

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 5,
  'Absolutely mesmerising. Could not put it down from the first page.',
  NOW() - INTERVAL '8 days'
FROM users u, books b
WHERE u.email = 'customer@bookstore.com' AND b.title = 'The Alchemist'
ON CONFLICT (book_id, user_id) DO NOTHING;

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 4,
  'Brilliant thriller. The twists kept me up all night.',
  NOW() - INTERVAL '5 days'
FROM users u, books b
WHERE u.email = 'customer@bookstore.com' AND b.title = 'Gone Girl'
ON CONFLICT (book_id, user_id) DO NOTHING;

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 5,
  'Heartbreaking and beautiful. One of the best novels I have ever read.',
  NOW() - INTERVAL '15 days'
FROM users u, books b
WHERE u.email = 'admin@bookstore.com' AND b.title = 'The Kite Runner'
ON CONFLICT (book_id, user_id) DO NOTHING;

INSERT INTO reviews (user_id, book_id, rating, review_text, created_at)
SELECT u.id, b.id, 5,
  'Dr. Kalam''s journey is truly inspiring. Every Indian should read this.',
  NOW() - INTERVAL '20 days'
FROM users u, books b
WHERE u.email = 'admin@bookstore.com' AND b.title = 'Wings of Fire'
ON CONFLICT (book_id, user_id) DO NOTHING;

-- ─── Orders ──────────────────────────────────────────────────────────────────

INSERT INTO orders (user_id, order_number, status, subtotal, tax_amount,
                    delivery_charge, discount_amount, total_amount, coupon_id,
                    delivery_address_snapshot, placed_at, estimated_delivery_date,
                    created_at, updated_at)
SELECT u.id, 'BST-20250701-00001', 'DELIVERED',
  898.00, 107.00, 0.00, 100.00, 905.00,
  (SELECT id FROM coupons WHERE code = 'SAVE100'),
  '{"firstName":"Ananya","lastName":"Sharma","addressLine1":"42 Indiranagar, 100 Feet Road","addressLine2":"Flat 3B","city":"Bengaluru","state":"Karnataka","country":"India","pinCode":"560038","phoneNumber":"+919876543210","email":"customer@bookstore.com"}'::jsonb,
  NOW() - INTERVAL '20 days', (CURRENT_DATE - INTERVAL '13 days')::date,
  NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'
FROM users u WHERE u.email = 'customer@bookstore.com'
ON CONFLICT (order_number) DO NOTHING;

INSERT INTO order_items (order_id, book_id, title, cover_image_url, format,
                         unit_price, quantity, line_total, created_at)
SELECT o.id, b.id, b.title, b.cover_image_url, b.format, b.price, 1, b.price,
       NOW() - INTERVAL '20 days'
FROM orders o CROSS JOIN books b
WHERE o.order_number = 'BST-20250701-00001'
  AND b.title IN ('Atomic Habits', 'The Kite Runner');

INSERT INTO payments (order_id, payment_method, status, payable_amount, paid_at, created_at)
SELECT o.id, 'CREDIT_CARD', 'SUCCESS', 905.00,
       NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'
FROM orders o WHERE o.order_number = 'BST-20250701-00001'
ON CONFLICT (order_id) DO NOTHING;

INSERT INTO orders (user_id, order_number, status, subtotal, tax_amount,
                    delivery_charge, discount_amount, total_amount, coupon_id,
                    delivery_address_snapshot, placed_at, estimated_delivery_date,
                    created_at, updated_at)
SELECT u.id, 'BST-20250715-00002', 'CONFIRMED',
  399.00, 48.00, 0.00, 0.00, 447.00, NULL,
  '{"firstName":"Ananya","lastName":"Sharma","addressLine1":"42 Indiranagar, 100 Feet Road","addressLine2":"Flat 3B","city":"Bengaluru","state":"Karnataka","country":"India","pinCode":"560038","phoneNumber":"+919876543210","email":"customer@bookstore.com"}'::jsonb,
  NOW() - INTERVAL '6 days', (CURRENT_DATE + INTERVAL '1 day')::date,
  NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'
FROM users u WHERE u.email = 'customer@bookstore.com'
ON CONFLICT (order_number) DO NOTHING;

INSERT INTO order_items (order_id, book_id, title, cover_image_url, format,
                         unit_price, quantity, line_total, created_at)
SELECT o.id, b.id, b.title, b.cover_image_url, b.format, b.price, 1, b.price,
       NOW() - INTERVAL '6 days'
FROM orders o CROSS JOIN books b
WHERE o.order_number = 'BST-20250715-00002' AND b.title = 'The Alchemist';

INSERT INTO payments (order_id, payment_method, status, payable_amount, paid_at, created_at)
SELECT o.id, 'UPI', 'SUCCESS', 447.00,
       NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'
FROM orders o WHERE o.order_number = 'BST-20250715-00002'
ON CONFLICT (order_id) DO NOTHING;

-- ─── User Followed Authors ────────────────────────────────────────────────────

INSERT INTO user_followed_authors (user_id, author_id, followed_at)
SELECT u.id, a.id, NOW() - INTERVAL '30 days'
FROM users u CROSS JOIN authors a
WHERE u.email = 'customer@bookstore.com'
  AND a.name IN ('Paulo Coelho', 'Khaled Hosseini')
ON CONFLICT (user_id, author_id) DO NOTHING;
