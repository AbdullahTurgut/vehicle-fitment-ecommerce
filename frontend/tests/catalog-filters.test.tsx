import { describe, it, expect } from "vitest";
import { ProductList } from "@/types";

describe("Catalog Category & Filtering Logic", () => {
  const sampleProducts: ProductList[] = [
    {
      id: "prod-1",
      name: "Volkswagen Passat B8 3D Havuzlu Paspas",
      slug: "volkswagen-passat-b8-3d-havuzlu-paspas",
      sku: "PASSAT-PASPAS-01",
      basePrice: 2499.9,
      salePrice: 2249.9,
      effectivePrice: 2249.9,
      stockQuantity: 20,
      inStock: true,
      featured: true,
    },
    {
      id: "prod-2",
      name: "Volkswagen Passat B8 3D Bagaj Havuzu",
      slug: "volkswagen-passat-b8-3d-bagaj-havuzu",
      sku: "PASSAT-BAGAJ-01",
      basePrice: 1499.9,
      effectivePrice: 1499.9,
      stockQuantity: 15,
      inStock: true,
      featured: true,
    },
    {
      id: "prod-3",
      name: "BMW 3 Serisi G20 3D Havuzlu Paspas",
      slug: "bmw-3-g20-3d-havuzlu-paspas",
      sku: "BMW-G20-PASPAS-01",
      basePrice: 2699.9,
      effectivePrice: 2699.9,
      stockQuantity: 10,
      inStock: true,
      featured: false,
    },
  ];

  it("filters all products when no category is selected", () => {
    const filterCategory = "";
    const filtered = sampleProducts.filter((p) => {
      if (!filterCategory) return true;
      if (filterCategory === "3d-oto-paspas") return p.slug.includes("paspas");
      if (filterCategory === "bagaj-havuzu") return p.slug.includes("bagaj");
      return true;
    });

    expect(filtered.length).toBe(3);
  });

  it("correctly filters 3D floor mat category (3d-oto-paspas)", () => {
    const filterCategory = "3d-oto-paspas";
    const filtered = sampleProducts.filter((p) => {
      if (filterCategory === "3d-oto-paspas") return p.slug.includes("paspas");
      if (filterCategory === "bagaj-havuzu") return p.slug.includes("bagaj");
      return true;
    });

    expect(filtered.length).toBe(2);
    expect(filtered.every((p) => p.slug.includes("paspas"))).toBe(true);
  });

  it("correctly filters trunk mat category (bagaj-havuzu)", () => {
    const filterCategory = "bagaj-havuzu";
    const filtered = sampleProducts.filter((p) => {
      if (filterCategory === "3d-oto-paspas") return p.slug.includes("paspas");
      if (filterCategory === "bagaj-havuzu") return p.slug.includes("bagaj");
      return true;
    });

    expect(filtered.length).toBe(1);
    expect(filtered[0].slug).toBe("volkswagen-passat-b8-3d-bagaj-havuzu");
  });

  it("filters simultaneously by category and search term", () => {
    const filterCategory = "3d-oto-paspas";
    const searchTerm = "bmw";

    const filtered = sampleProducts.filter((p) => {
      const matchCat =
        !filterCategory ||
        (filterCategory === "3d-oto-paspas" && p.slug.includes("paspas")) ||
        (filterCategory === "bagaj-havuzu" && p.slug.includes("bagaj"));

      const matchSearch =
        !searchTerm ||
        p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        p.sku.toLowerCase().includes(searchTerm.toLowerCase());

      return matchCat && matchSearch;
    });

    expect(filtered.length).toBe(1);
    expect(filtered[0].name).toContain("BMW");
    expect(filtered[0].slug).toContain("paspas");
  });

  it("generates correct URL query params on category change", () => {
    const buildCategoryQuery = (currentSearch: string, newCategory: string) => {
      const params = new URLSearchParams(currentSearch);
      if (newCategory) {
        params.set("category", newCategory);
      } else {
        params.delete("category");
      }
      params.delete("page");
      const q = params.toString();
      return q ? `/katalog?${q}` : "/katalog";
    };

    expect(buildCategoryQuery("", "bagaj-havuzu")).toBe("/katalog?category=bagaj-havuzu");
    expect(buildCategoryQuery("category=bagaj-havuzu", "")).toBe("/katalog");
    expect(buildCategoryQuery("search=passat&page=2", "3d-oto-paspas")).toBe(
      "/katalog?search=passat&category=3d-oto-paspas"
    );
  });
});
