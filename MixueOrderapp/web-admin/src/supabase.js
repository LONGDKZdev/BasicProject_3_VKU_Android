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
  const cleanName = file.name.replace(/[^a-zA-Z0-9.\-_]/g, '');
  const path = `${folder}/${productId}/${cleanName}`;

const endpoint = `${baseUrl}/storage/v1/object/${encodeURIComponent(SUPABASE.bucket)}/${path}`;
  const res = await fetch(endpoint, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${SUPABASE.anonKey}`,
      apikey: SUPABASE.anonKey,
      "x-upsert": "false", // 3. Set FALSE: Nếu ảnh đã tồn tại, sẽ trả về mã lỗi 409
      "Content-Type": file.type || "application/octet-stream",
    },
    body: file,
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");

    // 4. Kiểm tra ảnh trùng lặp: Bắt lỗi 409 hoặc thông báo "already exists"
    if (res.status === 409 || text.includes("already exists") || text.includes("Duplicate")) {
      console.log("Ảnh đã tồn tại do trùng tên. Hệ thống tự động tái sử dụng URL cũ.");
    } else {
      throw new Error(`Supabase upload failed (HTTP ${res.status}): ${text}`);
    }
  }

  const publicUrl = `${baseUrl}/storage/v1/object/public/${SUPABASE.bucket}/${path}`;
  return { publicUrl, path };
}

