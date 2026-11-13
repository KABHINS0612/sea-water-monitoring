import http from 'http';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 3000;
const BACKEND_URL = 'http://127.0.0.1:8080';

const MIME_TYPES = {
  '.html': 'text/html',
  '.css': 'text/css',
  '.js': 'text/javascript',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon'
};

const server = http.createServer((req, res) => {
  const cleanUrl = req.url.split('?')[0];

  // Proxy /api requests to Spring Boot backend
  if (cleanUrl.startsWith('/api')) {
    const proxyReq = http.request(
      `${BACKEND_URL}${req.url}`,
      {
        method: req.method,
        headers: req.headers
      },
      (proxyRes) => {
        res.writeHead(proxyRes.statusCode, proxyRes.headers);
        proxyRes.pipe(res, { end: true });
      }
    );

    proxyReq.on('error', (err) => {
      res.writeHead(502, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        error: 'Bad Gateway',
        message: 'Could not connect to Spring Boot backend on port 8080. Ensure java-backend is running.',
        detail: err.message
      }));
    });

    req.pipe(proxyReq, { end: true });
    return;
  }

  // Serve static files
  let safePath = cleanUrl === '/' ? '/index.html' : cleanUrl;
  let filePath = path.join(__dirname, safePath);

  // Prevent directory traversal
  if (!filePath.startsWith(__dirname)) {
    res.writeHead(403);
    res.end('Forbidden');
    return;
  }

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      filePath = path.join(__dirname, 'index.html');
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    fs.readFile(filePath, (readErr, content) => {
      if (readErr) {
        res.writeHead(500);
        res.end('Internal Server Error');
        return;
      }
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(content, 'utf-8');
    });
  });
});

server.listen(PORT, () => {
  console.log(`==================================================================`);
  console.log(` SEA WATER MONITORING - FRONTEND SERVER RUNNING                   `);
  console.log(` Local URL:     http://localhost:${PORT}                             `);
  console.log(` Proxying /api: ${BACKEND_URL}                                    `);
  console.log(`==================================================================`);
});
