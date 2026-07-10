const https = require('https');
const fs = require('fs');
const path = require('path');

const binId = 'a20ffed648ea679c5ce2';

function getDatabase() {
    return new Promise((resolve, reject) => {
        https.get(`https://api.npoint.io/${binId}`, (res) => {
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    resolve(JSON.parse(data));
                } catch (e) {
                    reject(e);
                }
            });
        }).on('error', reject);
    });
}

function updateDatabase(db) {
    return new Promise((resolve, reject) => {
        const payload = JSON.stringify(db);
        const req = https.request({
            hostname: 'api.npoint.io',
            path: `/${binId}`,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(payload)
            }
        }, (res) => {
            console.log('Update response status:', res.statusCode);
            let data = '';
            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                console.log('Update response body:', data.substring(0, 500));
                resolve(data);
            });
        });
        req.on('error', reject);
        req.write(payload);
        req.end();
    });
}

async function run() {
    try {
        let targetApkVersion = 73; // Fallback
        try {
            const mainActivityPath = path.join(__dirname, 'app/src/main/java/com/example/MainActivity.kt');
            if (fs.existsSync(mainActivityPath)) {
                const mainActivityContent = fs.readFileSync(mainActivityPath, 'utf8');
                const versionMatch = mainActivityContent.match(/const\s+val\s+CURRENT_APP_VERSION\s*=\s*(\d+)/);
                if (versionMatch) {
                    targetApkVersion = parseInt(versionMatch[1], 10);
                    console.log('Extracted CURRENT_APP_VERSION from MainActivity.kt:', targetApkVersion);
                }
            }
        } catch (err) {
            console.log('Could not read CURRENT_APP_VERSION from MainActivity, using fallback:', err.message);
        }

        let targetApkUrl = 'https://litter.catbox.moe/wwceap.apk'; // Fallback
        try {
            const uploadedLinkPath = path.join(__dirname, 'uploaded_link.txt');
            if (fs.existsSync(uploadedLinkPath)) {
                const uploadedLinkContent = fs.readFileSync(uploadedLinkPath, 'utf8');
                const lines = uploadedLinkContent.split('\n');
                const bodyLine = lines.find(l => l.startsWith('Body: '));
                if (bodyLine) {
                    const url = bodyLine.replace('Body: ', '').trim();
                    if (url.startsWith('http')) {
                        targetApkUrl = url;
                        console.log('Extracted targetApkUrl from uploaded_link.txt:', targetApkUrl);
                    }
                }
            }
        } catch (err) {
            console.log('Could not read uploaded_link.txt, using fallback URL:', err.message);
        }

        console.log('Fetching database from npoint...');
        const db = await getDatabase();
        console.log('Current DB configuration:');
        console.log('  App Name:', db.appConfig.appName);
        console.log('  latestApkVersion:', db.appConfig.latestApkVersion);
        console.log('  apkDownloadUrl:', db.appConfig.apkDownloadUrl);
        console.log('  dbVersion:', db.appConfig.dbVersion);
        console.log('  forceShowUpdateBanner:', db.appConfig.forceShowUpdateBanner);

        // Calculate targetDbVersion dynamically by incrementing existing dbVersion by 1
        const targetDbVersion = (db.appConfig.dbVersion || 315) + 1;

        // Update properties
        db.appConfig.latestApkVersion = targetApkVersion;
        db.appConfig.apkDownloadUrl = targetApkUrl;
        db.appConfig.dbVersion = targetDbVersion; // Ensure DB version is in line
        db.appConfig.forceShowUpdateBanner = false; // Turn off forced update banner so updated users don't see it
        db.appConfig.scriptAdEnable = true;
        db.appConfig.scriptAdCode = `<script>
  atOptions = {
    'key' : 'c4519774ce210febdf21e641a14531cc',
    'format' : 'iframe',
    'height' : 50,
    'width' : 320,
    'params' : {}
  };
</script>
<script src="https://www.highperformanceformat.com/c4519774ce210febdf21e641a14531cc/invoke.js"></script>`;
        
        console.log('\nUpdating configuration to:');
        console.log('  latestApkVersion ->', targetApkVersion);
        console.log('  apkDownloadUrl ->', targetApkUrl);
        console.log('  dbVersion ->', targetDbVersion);
        console.log('  scriptAdEnable ->', true);

        await updateDatabase(db);
        console.log('Successfully updated DB on nPoint!');

    } catch (e) {
        console.error('Error:', e.message);
    }
}

run();
