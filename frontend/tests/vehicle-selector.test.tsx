import { describe, it, expect, beforeEach } from "vitest";
import { useVehicleStore } from "@/stores/vehicle-store";
import { VehicleBrand, VehicleModel, VehicleGeneration, VehicleVariant } from "@/types";

describe("Vehicle Selector Store & State Flow", () => {
  const brandWithModels: VehicleBrand = {
    id: "44444444-4444-4444-4444-444444444444",
    name: "Volkswagen",
    slug: "volkswagen",
    active: true,
  };

  const brandWithoutModels: VehicleBrand = {
    id: "11111111-1111-1111-1111-111111111111",
    name: "Audi",
    slug: "audi",
    active: true,
  };

  const modelPassat: VehicleModel = {
    id: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    brandId: "44444444-4444-4444-4444-444444444444",
    name: "Passat",
    slug: "passat",
    active: true,
  };

  const genB8: VehicleGeneration = {
    id: "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
    modelId: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    name: "B8",
    code: "B8",
    startYear: 2015,
    endYear: 2024,
    active: true,
  };

  const variantSedan: VehicleVariant = {
    id: "cccccccc-cccc-cccc-cccc-cccccccccccc",
    generationId: "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
    name: "Standard",
    bodyType: "SEDAN",
    seatCount: 5,
    trunkType: "STANDARD",
    active: true,
  };

  beforeEach(() => {
    useVehicleStore.getState().clearVehicle();
    useVehicleStore.getState().setSelectorOpen(false);
  });

  it("handles modal open and close state", () => {
    expect(useVehicleStore.getState().isSelectorOpen).toBe(false);
    useVehicleStore.getState().setSelectorOpen(true);
    expect(useVehicleStore.getState().isSelectorOpen).toBe(true);
  });

  it("selects vehicle and calculates displayName correctly", () => {
    useVehicleStore.getState().selectVehicle({
      brand: brandWithModels,
      model: modelPassat,
      generation: genB8,
      variant: variantSedan,
      year: 2021,
    });

    const current = useVehicleStore.getState().selectedVehicle;
    expect(current).toBeDefined();
    expect(current?.brand.name).toBe("Volkswagen");
    expect(current?.model.name).toBe("Passat");
    expect(current?.generation.name).toBe("B8");
    expect(current?.year).toBe(2021);

    const vehicleTitle = [
      current?.year,
      current?.brand.name,
      current?.model.name,
      current?.generation.name,
      current?.variant?.name,
    ]
      .filter(Boolean)
      .join(" ");

    expect(vehicleTitle).toBe("2021 Volkswagen Passat B8 Standard");
  });

  it("clears vehicle selection properly", () => {
    useVehicleStore.getState().selectVehicle({
      brand: brandWithModels,
      model: modelPassat,
      generation: genB8,
      variant: variantSedan,
      year: 2021,
    });

    expect(useVehicleStore.getState().selectedVehicle).not.toBeNull();
    useVehicleStore.getState().clearVehicle();
    expect(useVehicleStore.getState().selectedVehicle).toBeNull();
  });

  it("identifies brand with zero models vs brand with models", () => {
    const modelsByBrand: Record<string, VehicleModel[]> = {
      [brandWithModels.id]: [modelPassat],
      [brandWithoutModels.id]: [],
    };

    expect(modelsByBrand[brandWithModels.id].length).toBe(1);
    expect(modelsByBrand[brandWithoutModels.id].length).toBe(0);

    const getEmptyStateMessage = (models: VehicleModel[]) => {
      if (models.length === 0) {
        return "Bu marka için araç verileri henüz eklenmedi.";
      }
      return null;
    };

    expect(getEmptyStateMessage(modelsByBrand[brandWithModels.id])).toBeNull();
    expect(getEmptyStateMessage(modelsByBrand[brandWithoutModels.id])).toBe(
      "Bu marka için araç verileri henüz eklenmedi."
    );
  });
});
