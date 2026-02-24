# 📱 GamingBoost Android App

Aplicación Android para la plataforma GamingBoost.

La app está desarrollada en Kotlin con Jetpack Compose y se conecta al backend Laravel del proyecto.

---

## 🚀 Tecnologías

- Kotlin
- Jetpack Compose
- Retrofit
- Coroutines
- Custom Tabs (para checkout)

---

## ▶️ Cómo ejecutar el proyecto

### 1️⃣ Abrir en Android Studio

- Abrir Android Studio
- Click en "Open"
- Seleccionar esta carpeta del proyecto

---

### 2️⃣ Ejecutar el backend

En el proyecto Laravel:

```bash
php artisan serve --host=0.0.0.0 --port=8000
```

---

### 3️⃣ Base URL (Emulador)

La app usa:

```
http://10.0.2.2:8000/api
```

> 10.0.2.2 es la IP especial para que el emulador acceda al localhost de tu PC.

---

## 🔗 Deep Link

Después del pago el backend redirige a:

```
gamingboost://payment-result
```

La app detecta ese deep link y refresca automáticamente los pedidos.

---

## ✅ Funcionalidades

- Login con token (Sanctum)
- Ver catálogo de boosts
- Crear órdenes
- Checkout simulado
- Historial de pedidos
- Estados: Pagado / Pendiente / Cancelado
- Auto refresh tras pago

---

## 🧠 Backend API

Repositorio del backend:

https://github.com/Danny8ac/GamingBoost-

---

## 👨‍💻 Autor

Daniel Ochoa  
Proyecto académico – 2026