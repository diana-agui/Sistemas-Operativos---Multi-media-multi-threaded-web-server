Servidor HTTP Multihilo - Java
==============================

Arquitectura:
  - ServerSocket escucha en puerto 8080
  - Cada conexión genera un HandleRequestJob
  - Un ThreadPool de workers ejecuta los jobs
  - NGINX actua como reverse proxy HTTPS en puerto 443

Archivos soportados:
  - .html  → text/html
  - .txt   → text/plain
  - .png   → image/png
  - .jpg   → image/jpeg
  - .gif   → image/gif

Estructura de carpetas:
  /var/www/html/
  ├── index.html
  ├── pages/
  ├── images/
  └── text/
