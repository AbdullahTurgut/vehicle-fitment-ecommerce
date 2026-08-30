import { AnnouncementBar } from "@/components/layout/announcement-bar";
import { Header } from "@/components/layout/header";
import { Footer } from "@/components/layout/footer";
import { VehicleBar } from "@/components/vehicle/vehicle-bar";
import { VehicleSelectorModal } from "@/components/vehicle/vehicle-selector-modal";
import { CartDrawer } from "@/components/cart/cart-drawer";

export default function StorefrontLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground">
      <AnnouncementBar />
      <Header />
      <VehicleBar />
      <main className="flex-1">{children}</main>
      <Footer />
      <VehicleSelectorModal />
      <CartDrawer />
    </div>
  );
}
