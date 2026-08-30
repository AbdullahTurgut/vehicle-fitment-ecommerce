export const STORAGE_KEYS = {
  ACCESS_TOKEN: "carmats_access_token",
  USER: "carmats_user",
  GUEST_TOKEN: "carmats_guest_token",
  SELECTED_VEHICLE: "carmats_selected_vehicle",
  RECENT_VEHICLES: "carmats_recent_vehicles",
} as const;

export const ORDER_STATUS_LABELS: Record<string, { label: string; color: string }> = {
  PENDING_PAYMENT: { label: "Ödeme Bekliyor", color: "bg-amber-100 text-amber-800 border-amber-200" },
  PAID: { label: "Ödeme Alındı", color: "bg-blue-100 text-blue-800 border-blue-200" },
  PROCESSING: { label: "Hazırlanıyor", color: "bg-indigo-100 text-indigo-800 border-indigo-200" },
  SHIPPED: { label: "Kargoya Verildi", color: "bg-purple-100 text-purple-800 border-purple-200" },
  DELIVERED: { label: "Teslim Edildi", color: "bg-emerald-100 text-emerald-800 border-emerald-200" },
  CANCELLED: { label: "İptal Edildi", color: "bg-red-100 text-red-800 border-red-200" },
  REFUNDED: { label: "İade Edildi", color: "bg-gray-100 text-gray-800 border-gray-200" },
};

export const SHIPMENT_STATUS_LABELS: Record<string, { label: string; color: string }> = {
  CREATED: { label: "Kargo Oluşturuldu", color: "bg-slate-100 text-slate-800 border-slate-200" },
  PICKED_UP: { label: "Kurye Teslim Aldı", color: "bg-blue-100 text-blue-800 border-blue-200" },
  IN_TRANSIT: { label: "Taşıma Durumunda", color: "bg-indigo-100 text-indigo-800 border-indigo-200" },
  OUT_FOR_DELIVERY: { label: "Dağıtıma Çıktı", color: "bg-amber-100 text-amber-800 border-amber-200" },
  DELIVERED: { label: "Teslim Edildi", color: "bg-emerald-100 text-emerald-800 border-emerald-200" },
  FAILED_DELIVERY: { label: "Teslim Edilemedi", color: "bg-rose-100 text-rose-800 border-rose-200" },
  RETURNED: { label: "İade Edildi", color: "bg-gray-100 text-gray-800 border-gray-200" },
};

export const CARRIER_NAMES: Record<string, string> = {
  YURTICI: "Yurtiçi Kargo",
  ARAS: "Aras Kargo",
  MNG: "MNG Kargo",
  PTT: "PTT Kargo",
  MOCK: "Süratli Teslimat (Demo)",
};

export const REVIEW_STATUS_LABELS: Record<string, { label: string; color: string }> = {
  PENDING: { label: "Onay Bekliyor", color: "bg-amber-100 text-amber-800 border-amber-200" },
  APPROVED: { label: "Onaylandı", color: "bg-emerald-100 text-emerald-800 border-emerald-200" },
  REJECTED: { label: "Reddedildi", color: "bg-rose-100 text-rose-800 border-rose-200" },
};

export const PRODUCT_STATUS_LABELS: Record<string, { label: string; color: string }> = {
  DRAFT: { label: "Taslak", color: "bg-gray-100 text-gray-800 border-gray-200" },
  ACTIVE: { label: "Aktif / Satışta", color: "bg-emerald-100 text-emerald-800 border-emerald-200" },
  PASSIVE: { label: "Pasif", color: "bg-amber-100 text-amber-800 border-amber-200" },
  OUT_OF_STOCK: { label: "Tükendi", color: "bg-rose-100 text-rose-800 border-rose-200" },
};
