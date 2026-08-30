import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import React from "react";
import { AnnouncementBar } from "@/components/layout/announcement-bar";
import { Button } from "@/components/ui/button";

describe("Components Rendering", () => {
  it("renders AnnouncementBar with key guarantee messages", () => {
    render(<AnnouncementBar />);
    expect(screen.getByText(/1.000 TL Üzeri Ücretsiz Kargo/i)).toBeDefined();
    expect(screen.getByText(/%100 Araca Birebir Uyum Garantisi/i)).toBeDefined();
    expect(screen.getByText(/14 Gün Koşulsuz İade/i)).toBeDefined();
  });

  it("renders Button with different variants and states", () => {
    const { rerender } = render(<Button variant="accent">Satın Al</Button>);
    expect(screen.getByRole("button", { name: /Satın Al/i })).toBeDefined();

    rerender(<Button isLoading>Satın Al</Button>);
    const btn = screen.getByRole("button");
    expect(btn.hasAttribute("disabled")).toBe(true);
  });
});
