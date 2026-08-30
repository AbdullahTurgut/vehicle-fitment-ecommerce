import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { SelectedVehicle } from "@/types";

interface VehicleState {
  selectedVehicle: SelectedVehicle | null;
  recentVehicles: SelectedVehicle[];
  isSelectorOpen: boolean;
  setSelectorOpen: (open: boolean) => void;
  selectVehicle: (vehicle: SelectedVehicle) => void;
  clearVehicle: () => void;
  removeRecentVehicle: (id: string) => void;
}

export const useVehicleStore = create<VehicleState>()(
  persist(
    (set, get) => ({
      selectedVehicle: null,
      recentVehicles: [],
      isSelectorOpen: false,

      setSelectorOpen: (open) => set({ isSelectorOpen: open }),

      selectVehicle: (vehicle) => {
        const currentRecents = get().recentVehicles;
        // Avoid duplicates in recents
        const vehicleKey = `${vehicle.brand.id}-${vehicle.model.id}-${vehicle.generation.id}-${vehicle.variant?.id || ""}-${vehicle.year || ""}`;
        const filtered = currentRecents.filter((v) => {
          const k = `${v.brand.id}-${v.model.id}-${v.generation.id}-${v.variant?.id || ""}-${v.year || ""}`;
          return k !== vehicleKey;
        });

        set({
          selectedVehicle: vehicle,
          recentVehicles: [vehicle, ...filtered].slice(0, 5),
          isSelectorOpen: false,
        });
      },

      clearVehicle: () => set({ selectedVehicle: null }),

      removeRecentVehicle: (vehicleKey) => {
        set({
          recentVehicles: get().recentVehicles.filter((v) => {
            const k = `${v.brand.id}-${v.model.id}-${v.generation.id}-${v.variant?.id || ""}-${v.year || ""}`;
            return k !== vehicleKey;
          }),
        });
      },
    }),
    {
      name: "carmats_vehicle_storage",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        selectedVehicle: state.selectedVehicle,
        recentVehicles: state.recentVehicles,
      }),
    }
  )
);
