export interface VehicleBrand {
  id: string;
  name: string;
  slug: string;
  logoUrl?: string;
  sortOrder?: number;
}

export interface VehicleModel {
  id: string;
  brandId: string;
  name: string;
  slug: string;
}

export interface VehicleGeneration {
  id: string;
  modelId: string;
  name: string;
  codeName?: string;
  startYear: number;
  endYear?: number;
}

export interface VehicleVariant {
  id: string;
  generationId: string;
  name: string;
  bodyType?: string;
  fuelType?: string;
  seatCount?: number;
  trunkType?: string;
}

export interface SelectedVehicle {
  brand: VehicleBrand;
  model: VehicleModel;
  generation: VehicleGeneration;
  variant?: VehicleVariant;
  year?: number;
}
