# Supabase setup (Storage only)

This project uses Supabase **only** for image storage.

## Bucket
- Bucket name: `StorageImage_MixueAndroid`
- Bucket access: **public**

## Local configuration (NOT committed)
Add the following to `local.properties`:

```properties
SUPABASE_URL=https://vqupigbrkuucghnauwrb.supabase.co
SUPABASE_ANON_KEY=YOUR_KEY_HERE
SUPABASE_BUCKET=StorageImage_MixueAndroid
```

These values are exported to `BuildConfig` as:
- `BuildConfig.SUPABASE_URL`
- `BuildConfig.SUPABASE_ANON_KEY`
- `BuildConfig.SUPABASE_BUCKET`

## Public URL format
We build public URLs as:

```
{SUPABASE_URL}/storage/v1/object/public/{bucket}/{path}
```

Example:
```
https://vqupigbrkuucghnauwrb.supabase.co/storage/v1/object/public/StorageImage_MixueAndroid/products/abc/main.jpg
```

