import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";

interface CartState {
  guestToken: string | null;
  isCartDrawerOpen: boolean;
  itemCount: number;
  setCartDrawerOpen: (open: boolean) => void;
  setGuestToken: (token: string | null) => void;
  setItemCount: (count: number) => void;
  getOrCreateGuestToken: () => string;
}

function generateGuestToken(): string {
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return "guest-" + Math.random().toString(36).substring(2, 15) + "-" + Date.now().toString(36);
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      guestToken: null,
      isCartDrawerOpen: false,
      itemCount: 0,

      setCartDrawerOpen: (open) => set({ isCartDrawerOpen: open }),
      setGuestToken: (token) => set({ guestToken: token }),
      setItemCount: (count) => set({ itemCount: count }),

      getOrCreateGuestToken: () => {
        let token = get().guestToken;
        if (!token) {
          token = generateGuestToken();
          set({ guestToken: token });
        }
        return token;
      },
    }),
    {
      name: "carmats_cart_storage",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        guestToken: state.guestToken,
      }),
    }
  )
);
