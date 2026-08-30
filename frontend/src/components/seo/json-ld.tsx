import { ProductDetail } from "@/types";

export function ProductJsonLd({ product }: { product: ProductDetail }) {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || "http://localhost:3000";

  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "Product",
    name: product.name,
    image: product.images?.[0]?.imageUrl || "",
    description: product.description || product.shortDescription || product.name,
    sku: product.sku,
    brand: {
      "@type": "Brand",
      name: product.manufacturerBrand || "OtoPaspas",
    },
    offers: {
      "@type": "Offer",
      url: `${baseUrl}/urunler/${product.slug}`,
      priceCurrency: "TRY",
      price: product.effectivePrice,
      availability: product.inStock
        ? "https://schema.org/InStock"
        : "https://schema.org/OutOfStock",
      itemCondition: "https://schema.org/NewCondition",
    },
    ...(product.averageRating && {
      aggregateRating: {
        "@type": "AggregateRating",
        ratingValue: product.averageRating,
        reviewCount: product.reviewCount || 1,
      },
    }),
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
    />
  );
}

export function OrganizationJsonLd() {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || "http://localhost:3000";

  const jsonLd = {
    "@context": "https://schema.org",
    "@type": "Organization",
    name: "OtoPaspas",
    url: baseUrl,
    logo: `${baseUrl}/logo.png`,
    contactPoint: {
      "@type": "ContactPoint",
      telephone: "+90-850-000-0000",
      contactType: "customer service",
      areaServed: "TR",
      availableLanguage: "Turkish",
    },
  };

  return (
    <script
      type="application/ld+json"
      dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
    />
  );
}
