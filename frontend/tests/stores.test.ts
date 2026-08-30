import { describe, it, expect, beforeEach } from "vitest";
import { useVehicleStore } from "@/stores/vehicle-store";
import { useCartStore } from "@/stores/cart-store";
import { useAuthStore } from "@/stores/auth-store";

describe("Zustand Stores", () => {
  beforeEach(() => {
    useVehicleStore.getState().clearVehicle();
    useCartStore.getState().setGuestToken(null);
    useAuthStore.getState().logout();
  });

  it("selects and clears vehicle properly in vehicleStore", () => {
    const mockVehicle = {
      brand: { id: "b1", name: "Volkswagen", slug: "vw" },
      model: { id: "m1", brandId: "b1", name: "Passat", slug: "passat" },
      generation: { id: "g1", modelId: "m1", name: "B8", startYear: 2015, endYear: 2024 },
      year: 2021,
    };

    useVehicleStore.getState().selectVehicle(mockVehicle);
    expect(useVehicleStore.getState().selectedVehicle?.brand.name).toBe("Volkswagen");
    expect(useVehicleStore.getState().selectedVehicle?.year).toBe(2021);
    expect(useVehicleStore.getState().recentVehicles.length).toBe(1);

    useVehicleStore.getState().clearVehicle();
    expect(useVehicleStore.getState().selectedVehicle).toBeNull();
  });

  it("generates or reuses guest token in cartStore", () => {
    const token1 = useCartStore.getState().getOrCreateGuestToken();
    expect(token1).toBeDefined();
    expect(typeof token1).toBe("string");

    const token2 = useCartStore.getState().getOrCreateGuestToken();
    expect(token2).toBe(token1);
  });

  it("handles auth state and admin role check", () => {
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(useAuthStore.getState().isAdmin()).toBe(false);

    useAuthStore.getState().setAuth({
      accessToken: "token123",
      refreshToken: "refresh123",
      tokenType: "Bearer",
      expiresIn: 3600,
      user: {
        id: "u1",
        email: "admin@carmats.com",
        firstName: "Admin",
        lastName: "User",
        roles: ["ROLE_ADMIN"],
      },
    });

    expect(useAuthStore.getState().isAuthenticated).toBe(true);
    expect(useAuthStore.getState().isAdmin()).toBe(true);

    useAuthStore.getState().logout();
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
  });
});
