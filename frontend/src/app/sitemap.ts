import { MetadataRoute } from "next";

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL || "http://localhost:3000";

  return [
    {
      url: `${baseUrl}`,
      lastModified: new Date(),
      changeFrequency: "daily",
      priority: 1.0,
    },
    {
      url: `${baseUrl}/katalog`,
      lastModified: new Date(),
      changeFrequency: "daily",
      priority: 0.9,
    },
    {
      url: `${baseUrl}/katalog?category=3d-oto-paspas`,
      lastModified: new Date(),
      changeFrequency: "weekly",
      priority: 0.8,
    },
    {
      url: `${baseUrl}/katalog?category=bagaj-havuzu`,
      lastModified: new Date(),
      changeFrequency: "weekly",
      priority: 0.8,
    },
    {
      url: `${baseUrl}/siparis-takip`,
      lastModified: new Date(),
      changeFrequency: "monthly",
      priority: 0.5,
    },
  ];
}
