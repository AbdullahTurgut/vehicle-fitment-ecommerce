"use client";

import { useState } from "react";
import { ShoppingCart } from "lucide-react";

interface ImageWithFallbackProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  fallbackText?: string;
}

export function ImageWithFallback({
  src,
  alt = "",
  className = "",
  fallbackText = "Görsel Yakında",
  ...props
}: ImageWithFallbackProps) {
  const [hasError, setHasError] = useState(false);

  if (!src || hasError) {
    return (
      <div className="w-full h-full flex flex-col items-center justify-center text-slate-300 bg-slate-100 p-4 select-none">
        <ShoppingCart className="w-10 h-10 stroke-[1.5] text-slate-400" />
        <span className="text-[11px] font-medium text-slate-500 mt-2 text-center">
          {fallbackText}
        </span>
      </div>
    );
  }

  return (
    // eslint-disable-next-line @next/next/no-img-element
    <img
      src={src}
      alt={alt}
      className={className}
      onError={() => setHasError(true)}
      {...props}
    />
  );
}
