<?php
// Outputs Firebase Web config as JSON.
// Note: Firebase Web config is not a secret. Real security is enforced via Firebase Rules.

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store, no-cache, must-revalidate, max-age=0');

// Locate the Android google-services.json to reuse existing project values.
$googleServicesPath = __DIR__ . '/../app/google-services.json';
if (!file_exists($googleServicesPath)) {
    http_response_code(500);
    echo json_encode([
        'error' => 'google-services.json not found',
        'path' => $googleServicesPath,
    ], JSON_PRETTY_PRINT);
    exit;
}

$raw = file_get_contents($googleServicesPath);
$data = json_decode($raw, true);
if (!is_array($data)) {
    http_response_code(500);
    echo json_encode(['error' => 'Invalid google-services.json'], JSON_PRETTY_PRINT);
    exit;
}

$projectId = $data['project_info']['project_id'] ?? '';
$storageBucket = $data['project_info']['storage_bucket'] ?? '';
$senderId = $data['project_info']['project_number'] ?? '';
$apiKey = $data['client'][0]['api_key'][0]['current_key'] ?? '';

if ($projectId === '' || $apiKey === '') {
    http_response_code(500);
    echo json_encode([
        'error' => 'Missing project_id or api_key in google-services.json',
        'projectId' => $projectId,
        'apiKeyPresent' => $apiKey !== '',
    ], JSON_PRETTY_PRINT);
    exit;
}

// Derive common web fields.
// authDomain normally is: <projectId>.firebaseapp.com
$authDomain = $projectId . '.firebaseapp.com';

// appId: web app id is not in google-services.json. For local testing we can omit it.
// Firebase modular SDK works with apiKey/authDomain/projectId (appId recommended but not strictly required).
// If you have a Web App in Firebase Console, replace this with the real appId.
$appId = getenv('FIREBASE_WEB_APP_ID');
if ($appId === false || $appId === '') {
    $appId = 'REPLACE_WITH_WEB_APP_ID_IF_AVAILABLE';
}

echo json_encode([
    'apiKey' => $apiKey,
    'authDomain' => $authDomain,
    'projectId' => $projectId,
    'storageBucket' => $storageBucket,
    'messagingSenderId' => $senderId,
    'appId' => $appId,
], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);

