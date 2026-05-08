// Minimal Supabase Storage uploader (no npm, works with plain browser + fetch)
// NOTE: This uses anon key (client-side). Bucket should be public.

export const SUPABASE = {
  url: "https://vqupigbrkuucghnauwrb.supabase.co",
  anonKey: "sb_publishable_hJjVJNHKhXvwIT17Goi0Cw_4w3AQE9y",
  bucket: "StorageImage_MixueAndroid",
};

function cleanBaseUrl(url) {
  return String(url || "")
    .trim()
    .replace(/\/+$/, "")
    .replace(/\/rest\/v1\/?$/, "");
}

export async function uploadProductImage({
  file,
  productId,
  folder = "products",
}) {
  if (!file) throw new Error("No file selected");
  if (!productId) throw new Error("productId is required");

  const baseUrl = cleanBaseUrl(SUPABASE.url);
  const path = `${folder}/${productId}/main_${Date.now()}_${file.name}`;

  const endpoint = `${baseUrl}/storage/v1/object/${encodeURIComponent(SUPABASE.bucket)}/${path}`;
  const res = await fetch(endpoint, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${SUPABASE.anonKey}`,
      apikey: SUPABASE.anonKey,
      "x-upsert": "true",
      "Content-Type": file.type || "application/octet-stream",
    },
    body: file,
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`Supabase upload failed (HTTP ${res.status}): ${text}`);
  }

  const publicUrl = `${baseUrl}/storage/v1/object/public/${SUPABASE.bucket}/${path}`;
  return { publicUrl, path };
}

