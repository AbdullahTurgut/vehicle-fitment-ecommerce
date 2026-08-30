import { describe, it, expect } from "vitest";
import { formatPrice, slugify, cn } from "@/lib/utils";

describe("Utils & Helpers", () => {
  it("formats price correctly for Turkish Lira", () => {
    const formatted = formatPrice(1250.5);
    const normalized = formatted.replace(/\u00A0/g, " ");
    expect(normalized).toContain("1.250,50");
    expect(normalized).toContain("₺");
  });

  it("handles zero and null prices gracefully", () => {
    expect(formatPrice(0).replace(/\u00A0/g, " ")).toContain("0,00");
    expect(formatPrice(null).replace(/\u00A0/g, " ")).toContain("0,00");
    expect(formatPrice(undefined).replace(/\u00A0/g, " ")).toContain("0,00");
  });

  it("slugifies Turkish strings correctly", () => {
    expect(slugify("Volkswagen Passat B8 3D Havuzlu Paspas")).toBe(
      "volkswagen-passat-b8-3d-havuzlu-paspas"
    );
    expect(slugify("Şık & Çağdaş Ürünler / Çözümler")).toBe(
      "sik-cagdas-urunler-cozumler"
    );
  });

  it("merges Tailwind CSS classes with cn", () => {
    const result = cn("p-4 text-sm", "p-6", { "bg-red-500": true, "hidden": false });
    expect(result).toContain("p-6");
    expect(result).toContain("text-sm");
    expect(result).toContain("bg-red-500");
    expect(result).not.toContain("p-4");
  });
});
