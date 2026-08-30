# Vehicle Fitment E-Commerce — Project Status

> Türkiye pazarına yönelik, araç uyumluluğu odaklı 3D oto paspas ve bagaj havuzu e-commerce platformu.

## Genel Durum

**Proje durumu:** Aktif geliştirme  
**Mevcut aşama:** ✅ Step 8 — User & Address Domain tamamlandı  
**Sıradaki ana aşama:** Step 9 — Cart Domain  
**Repository:** `vehicle-fitment-ecommerce`

---

## 1. Proje Hedefi

Bu proje; Türkiye'deki müşterilere yönelik, araç marka/model/kasa/yıl/varyant uyumluluğunu merkeze alan bir e-commerce platformudur.

Temel kullanıcı akışı:

```text
Kullanıcı
  ↓
Araç seçimi
  ↓
Marka
  ↓
Model
  ↓
Kasa / Generation
  ↓
Variant
  ↓
Model yılı
  ↓
Uyumlu ürünler
  ↓
Ürün detayı
  ↓
Sepet
  ↓
Checkout
  ↓
Ödeme
  ↓
Sipariş
  ↓
Kargo / Sipariş takibi
```

Ana ürün grupları:

- 3D Oto Paspas
- 3D Bagaj Havuzu
- İleride genişletilebilir aksesuar kategorileri

---

## 2. Temel Teknoloji Stack'i

### Backend

- Java 21 hedef sürümü
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Validation
- Spring Security
- PostgreSQL
- Flyway
- Spring Boot Actuator
- Springdoc OpenAPI / Swagger
- Maven
- Lombok
- REST API
- Modular Monolith mimarisi

### Frontend

> Henüz geliştirme başlamadı.

Planlanan stack:

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- Zustand
- Axios veya Fetch wrapper
- Responsive UI
- Modern component tabanlı mimari

### Infrastructure / DevOps

- Docker
- Docker Compose
- PostgreSQL container
- Redis — sonraki aşama
- MinIO / S3-compatible object storage — sonraki aşama
- Nginx — production reverse proxy
- `.env` tabanlı environment yönetimi
- `.env.example` repository'de tutulur
- Gerçek `.env` Git'e dahil edilmez

---

## 3. Backend Standartları

### API Base Path

```text
/api/v1
```

### Kullanıcıya Dönük Mesajlar

Proje Türkiye pazarına yönelik olduğu için:

- Backend exception mesajları Türkçe
- Validation mesajları Türkçe
- Business error mesajları Türkçe
- Swagger açıklamaları mümkün olduğunca Türkçe
- Teknik error code değerleri İngilizce

Örnek:

```json
{
  "status": 404,
  "code": "PRODUCT_NOT_FOUND",
  "message": "Ürün bulunamadı.",
  "errors": null,
  "timestamp": "..."
}
```

### Error Code Örnekleri

- `PRODUCT_NOT_FOUND`
- `CATEGORY_NOT_FOUND`
- `VEHICLE_BRAND_NOT_FOUND`
- `VEHICLE_MODEL_NOT_FOUND`
- `VEHICLE_GENERATION_NOT_FOUND`
- `VEHICLE_VARIANT_NOT_FOUND`
- `PRODUCT_NOT_COMPATIBLE`
- `INVALID_VEHICLE_YEAR`
- `VALIDATION_ERROR`
- `INTERNAL_SERVER_ERROR`

---

# 4. Tamamlanan Adımlar

## ✅ Step 1 — Project Architecture

Tamamlanan kararlar:

- Monorepo yapı
- Backend / Frontend / Infrastructure ayrımı
- Modular Monolith backend mimarisi
- Docker servis planı
- PostgreSQL ana veritabanı
- Redis ve MinIO sonraki aşamalara bırakıldı

Önerilen root yapı:

```text
vehicle-fitment-ecommerce/
├── backend/
├── frontend/
├── infrastructure/
├── docs/
├── scripts/
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

## ✅ Step 2 — PostgreSQL ER Diagram

Ana domainler belirlendi:

```text
User
Vehicle
Catalog
Cart
Order
Payment
Shipping
Marketing
Content
```

Araç uyumluluk modelinin çekirdeği:

```text
VehicleBrand
  ↓
VehicleModel
  ↓
VehicleGeneration
  ↓
VehicleVariant
  ↓
ProductCompatibility
  ↓
Product
```

E-commerce akışı:

```text
User
 ↓
Cart
 ↓
CartItem
 ↓
Product

User
 ↓
Order
 ├── OrderItem
 ├── Payment
 └── Shipment
```

---

## ✅ Step 3 — Spring Boot Backend Foundation

Tamamlananlar:

- Spring Boot uygulaması ayağa kaldırıldı
- PostgreSQL bağlantısı kuruldu
- Docker PostgreSQL çalışıyor
- Flyway aktif
- JPA / Hibernate aktif
- `ddl-auto: validate`
- Dev profile aktif
- Global exception sistemi hazır
- Türkçe error response standardı hazır
- Actuator aktif
- Swagger / SpringDoc aktif
- Security dependency hazır

Mevcut durum:

```text
Spring Boot   ✓
PostgreSQL    ✓
Flyway        ✓
Hibernate     ✓
Actuator      ✓
Swagger       ✓
```

---

## ✅ Step 4 — Vehicle Domain

Tamamlanan entity'ler:

- `VehicleBrand`
- `VehicleModel`
- `VehicleGeneration`
- `VehicleVariant`

Tamamlanan katmanlar:

- Entity
- Repository
- DTO
- Mapper
- Service
- Controller
- Seed data
- Türkçe exception handling

Public endpointler:

```text
GET /api/v1/vehicles/brands

GET /api/v1/vehicles/brands/{brandId}/models

GET /api/v1/vehicles/models/{modelId}/generations

GET /api/v1/vehicles/generations/{generationId}/variants
```

Test hiyerarşisi:

```text
Volkswagen
 ↓
Passat
 ↓
B8
 ↓
Standard
```

---

## ✅ Step 5 — Catalog Domain

Tamamlanan entity'ler:

- `Category`
- `Product`
- `ProductImage`
- `ProductFeature`
- `ProductCompatibility`
- `ProductStatus`

Ürün → Araç uyumluluk bağlantısı çalışıyor:

```text
Product
   ↓
ProductCompatibility
   ↓
VehicleVariant
```

Public katalog endpointleri:

```text
GET /api/v1/catalog/categories

GET /api/v1/catalog/products

GET /api/v1/catalog/products/{slug}

GET /api/v1/catalog/compatible-products
```

Seed ürünler:

- Volkswagen Passat B8 3D Havuzlu Paspas
- Volkswagen Passat B8 3D Bagaj Havuzu

Uyumluluk örneği:

```text
Volkswagen
 ↓
Passat
 ↓
B8
 ↓
Standard
 ↓
2015–2024
 ↓
Uyumlu paspas + bagaj havuzu
```

---

# 5. ✅ MEVCUT KONUM — Step 5.5 Catalog Hardening

> **Şu anda buradayız.**

Tamamlanan hardening başlıkları:

- ✅ Product pagination
- ✅ Category slug filtering
- ✅ `PageResponse<T>` standardı
- ✅ `ProductListProjection`
- ✅ Public product query
- ✅ Primary image join
- ✅ Product list tarafındaki N+1 azaltma
- ✅ `effectivePrice`
- ✅ `inStock`
- ✅ Product detail active category kontrolü
- ✅ Vehicle generation year validation
- ✅ Türkçe query parameter validation
- ✅ Invalid vehicle year business exception
- ✅ Primary image unique constraint tasarımı
- ✅ Swagger / OpenAPI açıklama planı
- ✅ Public endpoint testleri

Güncel ürün liste endpoint'i:

```text
GET /api/v1/catalog/products
    ?page=0
    &size=12
    &category=3d-oto-paspas
```

Güncel uyumluluk endpoint'i:

```text
GET /api/v1/catalog/compatible-products
    ?variantId={variantId}
    &year=2021
```

Örnek validation:

```text
Passat B8
Generation: 2015–2024

2021 → geçerli
2010 → INVALID_VEHICLE_YEAR
2025 → INVALID_VEHICLE_YEAR
```

---

## ✅ Step 5.5.26 — Compatible Products Query Optimization

Tamamlanan optimizasyonlar:
- ✅ Compatible products akışındaki per-product N+1 görsel sorgusu tamamen kaldırıldı.
- ✅ `ProductCompatibilityRepository.findCompatibleProducts` metodu `ProductListProjection` dönecek şekilde native query'ye refactor edildi.
- ✅ Primary image bilgisi uyumluluk sorgusunda `LEFT JOIN product_images` ile doğrudan alındı.
- ✅ Mevcut API davranışı, doğrulama kuralları (`INVALID_VEHICLE_YEAR`), opsiyonel model yılı desteği ve response kontratı korundu.
- ✅ Görseli olmayan ürünlerin de response'ta hatasız şekilde listelenmesi garanti altına alındı.
- ✅ Odaklı regression testleri eklendi (`CatalogServiceTest`, `CatalogCompatibleProductsIntegrationTest`).
- ✅ Güncel test sonuçları: **12 tests, 0 failures, 0 errors, 0 skipped — BUILD SUCCESS**.

---

# 6. Admin Catalog Management (Tamamlandı)

## ✅ Step 6.1 — Admin Category Management (Tamamlandı)

Tamamlanan admin kategori yönetim endpointleri ve özellikleri:
- ✅ `GET /api/v1/admin/categories` (Tüm aktif ve pasif kategorileri listeleme)
- ✅ `GET /api/v1/admin/categories/{id}` (Kategori detayını ID ile getirme)
- ✅ `POST /api/v1/admin/categories` (Yeni kategori oluşturma, slug çakışma ve parent kontrolü)
- ✅ `PUT /api/v1/admin/categories/{id}` (Kategori bilgilerini güncelleme, self-parent engeli)
- ✅ `PATCH /api/v1/admin/categories/{id}/status` (Kategori aktif/pasif durumunu güncelleme)
- ✅ Otomatik ve manuel Türkçe karakter uyumlu URL slug yönetimi (`SlugUtils`)
- ✅ Modüler servis ve controller mimarisi (`AdminCategoryController`, `AdminCategoryService`)
- ✅ Admin DTO ve validasyon standartları (`CreateCategoryRequest`, `UpdateCategoryRequest`, `UpdateCategoryStatusRequest`, `AdminCategoryResponse`)
- ✅ Kapsamlı unit ve entegrasyon testleri (`AdminCategoryServiceTest`, `AdminCategoryControllerIntegrationTest`)

---

## ✅ Step 6.2 – 6.5 — Admin Product, Image, Feature & Compatibility Management (Tamamlandı)

Admin tarafında SQL seed bağımlılığı tamamen kaldırıldı ve tam CRUD/ilişki yönetimi sağlandı:

Tamamlanan admin ürün ve alt kaynak yönetim endpointleri:
- ✅ `GET /api/v1/admin/products` (Sayfalanabilir, kategori ve durum filtreli, arama destekli admin ürün listesi)
- ✅ `GET /api/v1/admin/products/{id}` (Görselleri, özellikleri ve araç uyumlulukları ile tam ürün detayı)
- ✅ `POST /api/v1/admin/products` (Yeni ürün oluşturma, slug/SKU tekillik kontrolü, fiyat doğrulama)
- ✅ `PUT /api/v1/admin/products/{id}` (Ürün bilgilerini güncelleme)
- ✅ `PATCH /api/v1/admin/products/{id}/status` (Ürün durumunu güncelleme: DRAFT, ACTIVE, PASSIVE, OUT_OF_STOCK)
- ✅ `POST /api/v1/admin/products/{id}/images` (Ürüne görsel ekleme, birincil görsel otomatik yönetimi)
- ✅ `DELETE /api/v1/admin/products/{id}/images/{imageId}` (Ürün görseli silme)
- ✅ `POST /api/v1/admin/products/{id}/features` (Ürüne özellik ekleme: başlık, açıklama, ikon, sıralama)
- ✅ `DELETE /api/v1/admin/products/{id}/features/{featureId}` (Ürün özelliği silme)
- ✅ `POST /api/v1/admin/products/{id}/compatibilities` (Araç varyantı ve model yılı aralığı ile ürün eşleştirme, kasa yılı sınır kontrolü)
- ✅ `DELETE /api/v1/admin/products/{id}/compatibilities/{compatibilityId}` (Araç uyumluluk kaydı silme)
- ✅ Kapsamlı unit ve entegrasyon testleri (`AdminProductServiceTest`, `AdminProductControllerIntegrationTest`)
- ✅ Güncel test sonuçları: **50 tests, 0 failures, 0 errors, 0 skipped — BUILD SUCCESS**

---

# 7. Authentication & Authorization (Tamamlandı)

## ✅ Step 7 — Authentication & Authorization (Tamamlandı)

Tamamlanan kimlik doğrulama, yetkilendirme ve güvenlik özellikleri:
- ✅ Spring Boot 4.1.1 & Spring Security 6+ güncel standartları
- ✅ Flyway `V8__create_user_and_auth_schema.sql` (users, roles, user_roles, refresh_tokens tabloları ve varsayılan roller / admin seed kaydı)
- ✅ `Role.java`, `User.java`, `RefreshToken.java` JPA entity'leri ve repository'leri
- ✅ JJWT 0.12.6 ile stateless token yönetimi (`JwtService`)
- ✅ `POST /api/v1/auth/register` (Yeni müşteri kaydı ve otomatik token üretimi)
- ✅ `POST /api/v1/auth/login` (E-posta ve şifre doğrulaması, access + refresh token)
- ✅ `POST /api/v1/auth/refresh` (Refresh token rotasyonu ve yeni access token)
- ✅ `GET /api/v1/auth/me` (Giriş yapmış kullanıcının profil ve rol bilgisi)
- ✅ `ROLE_ADMIN` rol yetkilendirmesi ile `/api/v1/admin/**` rotalarının mutlak korunması
- ✅ Statik ve açık uçlar (`/api/v1/public/**`, `/api/v1/vehicles/**`, `/api/v1/catalog/**`, `/swagger-ui/**`, `/actuator/**`) için `permitAll`
- ✅ Standart JSON hata yanıtları: 401 Unauthorized (`CustomAuthenticationEntryPoint`) ve 403 Forbidden (`CustomAccessDeniedHandler`)
- ✅ Kapsamlı unit ve entegrasyon testleri (`JwtServiceTest`, `AuthServiceTest`, `AuthControllerIntegrationTest`)
- ✅ Güncel test sonuçları: **66 tests, 0 failures, 0 errors, 0 skipped — BUILD SUCCESS**

---

# 8. User & Address Domain (Tamamlandı)

## ✅ Step 8 — User & Address Domain (Tamamlandı)

Tamamlanan kullanıcı profili ve adres yönetimi özellikleri:
- ✅ Flyway `V9__create_address_schema.sql` (addresses tablosu, teslimat/fatura varsayılan flagleri, foreign key ve indeksler)
- ✅ `Address.java` JPA entity'si ve `AddressRepository.java` (kullanıcı bazlı adres sorgulama, varsayılan adres flag sıfırlama)
- ✅ `UserService.java` & `UserController.java`:
  - `GET /api/v1/users/profile` (Oturum açmış kullanıcının profil bilgileri)
  - `PUT /api/v1/users/profile` (Ad, soyad, telefon güncelleme)
  - `PATCH /api/v1/users/password` (Eski şifre doğrulaması ile şifre değiştirme)
- ✅ `AddressService.java` & `AddressController.java`:
  - `GET /api/v1/users/addresses` (Kullanıcının kayıtlı adres listesi)
  - `GET /api/v1/users/addresses/{id}` (Adres detayı ve sahiplik kontrolü)
  - `POST /api/v1/users/addresses` (Yeni adres ekleme; ilk adresi otomatik varsayılan yapma, teslimat/fatura varsayılan yönetimi)
  - `PUT /api/v1/users/addresses/{id}` (Adres güncelleme ve sahiplik kontrolü)
  - `DELETE /api/v1/users/addresses/{id}` (Adres silme ve sahiplik kontrolü)
  - `PATCH /api/v1/users/addresses/{id}/default` (Varsayılan teslimat/fatura adresi belirleme)
- ✅ Kapsamlı unit ve entegrasyon testleri (`UserServiceTest`, `AddressServiceTest`, `UserControllerIntegrationTest`, `AddressControllerIntegrationTest`)
- ✅ Güncel test sonuçları: **83 tests, 0 failures, 0 errors, 0 skipped — BUILD SUCCESS**

---

# 9. Sıradaki Backend Roadmap

## ⏭ Step 9 — Cart Domain (Sıradaki Aşama)

Plan:
- Sepet veri modeli (`carts`, `cart_items` tabloları ve JPA entity'leri)
- Misafir sepeti (`sessionId` / `guestToken`) ve oturum açmış kullanıcı sepeti desteği
- `GET /api/v1/cart` (Aktif sepeti, sepet kalemlerini, ara toplam ve toplam tutarı getirme)
- `POST /api/v1/cart/items` (Sepete ürün ekleme, aktiflik ve stok kontrolü)
- `PUT /api/v1/cart/items/{itemId}` (Sepetteki ürün adet güncelleme, stok sınırı kontrolü)
- `DELETE /api/v1/cart/items/{itemId}` (Sepetten ürün çıkarma)
- `DELETE /api/v1/cart` (Sepeti tamamen temizleme)
- `POST /api/v1/cart/merge` (Kullanıcı giriş yaptığında misafir sepetini kullanıcı sepetine aktarma/birleştirme)
- Sepet hesaplamaları, stok validasyonları ve entegrasyon testleri

---

## Step 10 — Checkout

Plan:

```text
Sepet
 ↓
İletişim
 ↓
Adres
 ↓
Kargo
 ↓
Ödeme
 ↓
Sipariş
```

Checkout sırasında:

- Fiyat tekrar doğrulanacak
- Stok tekrar doğrulanacak
- Ürün aktiflik durumu kontrol edilecek
- Araç uyumluluğu gerekiyorsa tekrar doğrulanabilecek

---

## Step 11 — Order Domain

Planlanan durumlar:

```text
PENDING_PAYMENT
PAID
PREPARING
SHIPPED
DELIVERED
CANCELLED
REFUNDED
```

OrderItem snapshot alanları:

- product name
- SKU
- quantity
- unit price
- total price

Sipariş adresi snapshot olarak saklanacak.

---

## Step 12 — Payment Integration

Provider abstraction kullanılacak.

Planlanan sağlayıcılar:

- iyzico
- PayTR

Interface mantığı:

```text
PaymentProvider
 ├── initializePayment
 ├── verifyPayment
 └── refund
```

---

## Step 13 — Shipping

İlk sürüm:

- Admin tarafından kargo firması girme
- Takip numarası
- Shipment status

Sonraki sürüm:

- Kargo API entegrasyonu

---

## Step 14 — Campaign / Coupon

Plan:

- Yüzde indirim
- Sabit tutar indirim
- Minimum sepet tutarı
- Maksimum indirim
- Kullanım limiti
- Başlangıç / bitiş zamanı

---

## Step 15 — Review & Favorites

Plan:

- Favori ürünler
- Ürün değerlendirmeleri
- Rating
- Yorum
- Admin onayı
- Doğrulanmış alışveriş etiketi

---

# 7. Frontend Roadmap

## Step F1 — React Foundation

- React
- TypeScript
- Vite
- Folder architecture
- Environment config
- API client
- React Router
- TanStack Query
- Zustand

---

## Step F2 — Global Layout

Componentler:

```text
AnnouncementBar
Header
MainNavigation
MobileMenu
Footer
WhatsAppButton
```

---

## Step F3 — Design System

Plan:

- Typography
- Button variants
- Inputs
- Select
- Badge
- Modal
- Card
- Spacing
- Responsive breakpoints
- Loading / Error / Empty states

Referanslardan alınan yaklaşım:

- Sahler → araç seçimi / e-commerce flow
- Rizline → araç uyumluluğu / ürün anlatımı / güven mesajları

---

## Step F4 — Home Page

Plan:

```text
Announcement Bar
Header
Hero
Vehicle Selector
Categories
Featured Products
Why Us
Trust Bar
Campaign Banner
Customer Reviews
SEO Content
Footer
```

---

## Step F5 — Vehicle Selector

Akış:

```text
Marka
 ↓
Model
 ↓
Generation
 ↓
Variant
 ↓
Yıl
 ↓
Uyumlu ürünleri getir
```

Seçilen araç Zustand + localStorage ile tutulabilir.

---

## Step F6 — Product Listing

Özellikler:

- Pagination
- Category filter
- Compatible products
- Product cards
- Primary image
- Effective price
- Stock badge
- Compatibility badge

Örnek:

```text
✓ Aracınıza Uygun
```

---

## Step F7 — Product Detail

Plan:

- Gallery
- Product title
- SKU
- Price
- Sale price
- Stock state
- Compatibility state
- Product features
- Add to cart
- WhatsApp CTA
- Shipping / return info
- Reviews
- Related products

---

## Step F8 — Cart UI

Plan:

- Cart drawer
- Cart page
- Quantity selector
- Remove item
- Price summary
- Coupon
- Checkout button

---

## Step F9 — Checkout UI

Plan:

- Contact
- Address
- Shipping
- Payment
- Order summary
- Error handling

---

## Step F10 — Account

Plan:

- Login
- Register
- Profile
- Addresses
- Orders
- Order detail
- Favorites

---

## Step F11 — Admin Panel

Planlanan bölümler:

```text
Dashboard
Products
Categories
Vehicles
Compatibilities
Orders
Users
Campaigns
Coupons
Content
Settings
```

---

# 8. Infrastructure Roadmap

## Step I1 — PostgreSQL

**Durum:** ✅ Tamamlandı

- Docker
- Persistent volume
- Healthcheck
- Flyway

---

## Step I2 — Redis

Planlanan kullanım:

- Cache
- Vehicle selector cache
- Rate limiting
- İleride session/cart desteği

---

## Step I3 — Object Storage

Plan:

- MinIO development
- S3 / Cloudflare R2 production opsiyonu

Ürün fotoğrafları DB'de tutulmayacak.

DB sadece URL saklayacak.

---

## Step I4 — Nginx

Production routing:

```text
/        → React
/api/*   → Spring Boot
```

---

## Step I5 — Dockerize Full Stack

Planlanan servisler:

```text
frontend
backend
postgres
redis
minio
nginx
```

---

## Step I6 — CI/CD

Plan:

- Maven test/build
- Frontend lint/build
- Docker image build
- GitHub Actions
- Deployment pipeline

---

# 9. SEO Roadmap

Araç bazlı SEO önemli olacak.

Örnek URL'ler:

```text
/volkswagen/passat/b8/3d-paspas

/volkswagen/passat/b8/bagaj-havuzu
```

Plan:

- SEO landing pages
- Vehicle + category pages
- Metadata
- Structured data
- Sitemap
- Canonical URLs
- Blog

---

# 10. Production Öncesi Kontrol Listesi

Backend:

- [ ] Authentication
- [ ] Authorization
- [ ] Cart
- [ ] Checkout
- [ ] Orders
- [ ] Payment
- [ ] Shipment
- [ ] Admin APIs
- [ ] Image upload
- [ ] Pagination / filtering
- [ ] Validation
- [ ] Error handling
- [ ] Logging
- [ ] Security hardening
- [ ] Rate limiting
- [ ] Tests

Frontend:

- [ ] Responsive design
- [ ] Vehicle selector
- [ ] Catalog
- [ ] Product detail
- [ ] Cart
- [ ] Checkout
- [ ] Account
- [ ] Admin panel
- [ ] Loading states
- [ ] Error states
- [ ] Mobile UX
- [ ] SEO

Infrastructure:

- [x] PostgreSQL
- [x] Docker Compose başlangıcı
- [x] Flyway
- [ ] Redis
- [ ] MinIO
- [ ] Nginx
- [ ] Full Docker stack
- [ ] CI/CD
- [ ] Production deployment

---

# 11. Mevcut Milestone

```text
STEP 1   Project Architecture              ✅
STEP 2   PostgreSQL ER Design              ✅
STEP 3   Backend Foundation                ✅
STEP 4   Vehicle Domain                    ✅
STEP 5   Catalog Domain                    ✅
STEP 5.5 Catalog Hardening                 ✅
STEP 5.5.26 Compatibility Query Optimize   ✅
STEP 6   Admin Catalog Management          ✅
STEP 7   Authentication & Authorization    ✅
STEP 8   User / Address                    ✅  ← TAMAMLANDI
STEP 9   Cart                              ⏳  ← SIRADAKİ ANA AŞAMA
STEP 10  Checkout                          ⏳
STEP 11  Order                             ⏳
STEP 12  Payment                           ⏳
STEP 13  Shipping                          ⏳
STEP 14  Campaign / Coupon                 ⏳
STEP 15  Review / Favorites                ⏳

FRONTEND FOUNDATION                        ⏳
STORE UI                                   ⏳
ADMIN UI                                   ⏳
FULL DOCKER STACK                          ⏳
CI/CD                                      ⏳
PRODUCTION RELEASE                         ⏳
```

---

# 12. Sonraki Teknik Adım

Bir sonraki geliştirme adımı:

> **Step 9 — Cart Domain**

Bu aşamada misafir (`guestId` / `guestToken`) ve oturum açmış kullanıcı sepeti yönetimi, ürün ekleme/adet güncelleme, stok doğrulama, sepet temizleme ve login sonrası sepet birleştirme (`merge`) REST API'leri geliştirilecektir.
