import { apiClient } from "@/lib/api/client";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import { VehicleBrand, VehicleModel, VehicleGeneration, VehicleVariant } from "@/types";

export const vehicleApi = {
  getBrands: async (): Promise<VehicleBrand[]> => {
    return apiClient<VehicleBrand[]>(API_ENDPOINTS.VEHICLE_BRANDS, {
      skipAuth: true,
    });
  },

  getModels: async (brandId: string): Promise<VehicleModel[]> => {
    return apiClient<VehicleModel[]>(API_ENDPOINTS.VEHICLE_MODELS(brandId), {
      skipAuth: true,
    });
  },

  getGenerations: async (modelId: string): Promise<VehicleGeneration[]> => {
    return apiClient<VehicleGeneration[]>(API_ENDPOINTS.VEHICLE_GENERATIONS(modelId), {
      skipAuth: true,
    });
  },

  getVariants: async (generationId: string): Promise<VehicleVariant[]> => {
    return apiClient<VehicleVariant[]>(API_ENDPOINTS.VEHICLE_VARIANTS(generationId), {
      skipAuth: true,
    });
  },
};
