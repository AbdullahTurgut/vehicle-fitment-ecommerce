-- ==============================================================================
-- V18: Remove legacy hardcoded placeholder admin seed from initial migrations
-- ==============================================================================

DELETE FROM refresh_tokens WHERE user_id = '90000000-0000-0000-0000-000000000001';
DELETE FROM user_roles WHERE user_id = '90000000-0000-0000-0000-000000000001';
DELETE FROM addresses WHERE user_id = '90000000-0000-0000-0000-000000000001';
DELETE FROM product_reviews WHERE user_id = '90000000-0000-0000-0000-000000000001';
DELETE FROM favorites WHERE user_id = '90000000-0000-0000-0000-000000000001';
DELETE FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE user_id = '90000000-0000-0000-0000-000000000001');
DELETE FROM carts WHERE user_id = '90000000-0000-0000-0000-000000000001';
UPDATE orders SET user_id = NULL WHERE user_id = '90000000-0000-0000-0000-000000000001';
UPDATE coupon_usages SET user_id = NULL WHERE user_id = '90000000-0000-0000-0000-000000000001';
DELETE FROM users WHERE id = '90000000-0000-0000-0000-000000000001';
