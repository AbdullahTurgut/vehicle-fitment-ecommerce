-- ============================================
-- CATEGORIES
-- ============================================

INSERT INTO categories
(
    id,
    name,
    slug,
    active,
    sort_order
)
VALUES
    (
        '10000000-0000-0000-0000-000000000001',
        '3D Oto Paspas',
        '3d-oto-paspas',
        TRUE,
        10
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        'Bagaj Havuzu',
        'bagaj-havuzu',
        TRUE,
        20
    );


-- ============================================
-- PRODUCTS
-- ============================================

INSERT INTO products
(
    id,
    category_id,
    name,
    slug,
    sku,
    short_description,
    description,
    base_price,
    sale_price,
    stock_quantity,
    status,
    featured,
    manufacturer_brand,
    material
)
VALUES
    (
        '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000001',
        'Volkswagen Passat B8 3D Havuzlu Paspas',
        'volkswagen-passat-b8-3d-havuzlu-paspas',
        'PASSAT-B8-PASPAS-001',
        'Volkswagen Passat B8 araçlara özel 3D havuzlu paspas.',
        'Aracınıza özel ölçülerde tasarlanan, yüksek kenarlı ve kolay temizlenebilir 3D havuzlu paspas.',
        2499.90,
        2249.90,
        25,
        'ACTIVE',
        TRUE,
        'CarMats',
        'TPE'
    );

INSERT INTO products
(
    id,
    category_id,
    name,
    slug,
    sku,
    short_description,
    description,
    base_price,
    stock_quantity,
    status,
    featured,
    manufacturer_brand,
    material
)
VALUES
    (
        '20000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000002',
        'Volkswagen Passat B8 3D Bagaj Havuzu',
        'volkswagen-passat-b8-3d-bagaj-havuzu',
        'PASSAT-B8-BAGAJ-001',
        'Volkswagen Passat B8 araçlara özel bagaj havuzu.',
        'Bagajınızı sıvı, kir ve günlük kullanım kaynaklı hasarlardan koruyan araca özel bagaj havuzu.',
        1499.90,
        18,
        'ACTIVE',
        TRUE,
        'CarMats',
        'TPE'
    );


-- ============================================
-- PRODUCT IMAGES
-- ============================================

INSERT INTO product_images
(
    id,
    product_id,
    url,
    alt_text,
    sort_order,
    is_primary
)
VALUES
    (
        '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        '/images/products/passat-b8-paspas.jpg',
        'Volkswagen Passat B8 3D havuzlu paspas',
        10,
        TRUE
    ),
    (
        '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000002',
        '/images/products/passat-b8-bagaj-havuzu.jpg',
        'Volkswagen Passat B8 bagaj havuzu',
        10,
        TRUE
    );


-- ============================================
-- PRODUCT FEATURES
-- ============================================

INSERT INTO product_features
(
    id,
    product_id,
    title,
    description,
    sort_order
)
VALUES
    (
        '40000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        'Araca Özel Tasarım',
        'Volkswagen Passat B8 için özel ölçülerde üretilmiştir.',
        10
    ),
    (
        '40000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001',
        'Kokusuz TPE',
        'Dayanıklı ve kolay temizlenebilir TPE malzemeden üretilmiştir.',
        20
    ),
    (
        '40000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000002',
        'Yüksek Kenar Koruması',
        'Sıvı ve kirin bagaj yüzeyine ulaşmasını azaltır.',
        10
    );


-- ============================================
-- PRODUCT COMPATIBILITIES
-- ============================================

INSERT INTO product_compatibilities
(
    id,
    product_id,
    vehicle_variant_id,
    start_year,
    end_year,
    notes
)
VALUES
    (
        '50000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        2015,
        2024,
        'Volkswagen Passat B8 standart kasa için uyumludur.'
    ),
    (
        '50000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000002',
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        2015,
        2024,
        'Volkswagen Passat B8 standart bagaj yapısı için uyumludur.'
    );