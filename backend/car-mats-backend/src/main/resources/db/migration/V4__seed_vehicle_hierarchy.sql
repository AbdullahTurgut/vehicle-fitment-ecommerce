INSERT INTO vehicle_models
(
    id,
    brand_id,
    name,
    slug,
    active,
    sort_order
)
VALUES
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '44444444-4444-4444-4444-444444444444',
        'Passat',
        'passat',
        TRUE,
        10
    );

INSERT INTO vehicle_generations
(
    id,
    model_id,
    name,
    code,
    start_year,
    end_year,
    active,
    sort_order
)
VALUES
    (
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'B8',
        'B8',
        2015,
        2024,
        TRUE,
        10
    );

INSERT INTO vehicle_variants
(
    id,
    generation_id,
    name,
    body_type,
    fuel_type,
    seat_count,
    trunk_type,
    notes,
    active,
    sort_order
)
VALUES
    (
        'cccccccc-cccc-cccc-cccc-cccccccccccc',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Standard',
        'SEDAN',
        NULL,
        5,
        'STANDARD',
        'Standart Passat B8 sedan uyumluluk kaydı.',
        TRUE,
        10
    );