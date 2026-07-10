const fs = require('fs');
const https = require('https');
const path = require('path');

const srcPath = path.join(__dirname, 'app/build/outputs/apk/release/app-release.apk');
const destPath = path.join(__dirname, 'app-release.apk');

// Ensure the compiled file is copied to root of project
try {
    fs.copyFileSync(srcPath, destPath);
    console.log('Successfully copied APK to ' + destPath);
} catch (e) {
    console.error('Copy failed: ' + e.message);
}

// Native Multipart Form-Data Binary Post helper
function uploadFile(url, fieldName, filePath, contentType, extraFields = {}) {
    return new Promise((resolve, reject) => {
        const boundary = '----WebKitFormBoundary' + Math.random().toString(36).substring(2);
        const fileName = path.basename(filePath);
        const fileData = fs.readFileSync(filePath);

        const parts = [];
        for (const [key, val] of Object.entries(extraFields)) {
            parts.push(Buffer.from(
                `--${boundary}\r\nContent-Disposition: form-data; name="${key}"\r\n\r\n${val}\r\n`
            ));
        }

        parts.push(Buffer.from(
            `--${boundary}\r\nContent-Disposition: form-data; name="${fieldName}"; filename="${fileName}"\r\nContent-Type: ${contentType}\r\n\r\n`
        ));
        parts.push(fileData);
        parts.push(Buffer.from(`\r\n--${boundary}--\r\n`));

        const body = Buffer.concat(parts);
        const parsedUrl = new URL(url);

        const req = https.request({
            host: parsedUrl.hostname,
            path: parsedUrl.pathname + parsedUrl.search,
            method: 'POST',
            headers: {
                'Content-Type': 'multipart/form-data; boundary=' + boundary,
                'Content-Length': body.length,
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'
            }
        }, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => resolve({ statusCode: res.statusCode, body: data }));
        });

        req.on('error', e => reject(e));
        req.write(body);
        req.end();
    });
}

// Perform uploads in parallel
console.log('Uploading active binary bundle...');

// Tmpfiles upload (Replaced with 72-hour Litterbox upload to prevent fast expiration)
uploadFile('https://litterbox.catbox.moe/resources/internals/api.php', 'fileToUpload', destPath, 'application/vnd.android.package-archive', { reqtype: 'fileupload', time: '72h' })
    .then((res) => {
        const link = res.body.trim();
        console.log('Litterbox response status:', res.statusCode, 'body:', link);
        fs.writeFileSync(path.join(__dirname, 'uploaded_link.txt'), "Status: " + res.statusCode + "\nBody: " + link);
    })
    .catch(err => console.log('Litterbox error: ' + err.message));

// Catbox upload (disabled in compiler env)
/*
uploadFile('https://catbox.moe/user/api.php', 'fileToUpload', destPath, 'application/vnd.android.package-archive', { reqtype: 'fileupload' })
    .then((res) => {
        const link = res.body.trim();
        if (link.startsWith('http')) {
            console.log('CATBOX_DOWNLOAD_LINK: ' + link);
        } else {
            console.log('Catbox check: ' + link);
        }
    })
    .catch(err => console.log('Catbox error: ' + err.message));
*/
