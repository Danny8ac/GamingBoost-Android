===============================

PARTE 1 – BACKEND (agregar link del repo Android)

===============================



1\) Ir al repo backend:

cd "E:\\AllMyThings\\Danielito Apps\\gamingboost\_api"



2\) Abrir README:

notepad README.md



3\) Agregar esta sección en cualquier parte lógica (ej: antes de Instalación o después de Tecnologías):



\## 📱 Repositorio Android

\- https://github.com/Danny8ac/GamingBoost-Android



4\) Guardar y cerrar.



5\) Subir cambios:

git add README.md

git commit -m "Add Android repo link"

git push





===============================

PARTE 2 – ANDROID (crear README profesional)

===============================



1\) Ir al proyecto Android:

cd "E:\\AllMyThings\\AStudioSaving"



2\) Crear/abrir README:

notepad README.md



3\) Pegar TODO esto dentro del archivo:



\# 📱 GamingBoost Android App



App Android para GamingBoost (Kotlin + Jetpack Compose).  

Se conecta al mismo backend Laravel usado por la web.



---



\## ✅ Tecnologías

\- Kotlin

\- Jetpack Compose

\- Retrofit

\- Coroutines

\- Custom Tabs (Checkout)



---



\## ⚙️ Requisitos

\- Android Studio

\- Backend Laravel ejecutándose localmente



---



\## ▶️ Cómo ejecutar



\### 1) Abrir proyecto

En Android Studio: Open → selecciona esta carpeta del proyecto.



\### 2) Ejecutar backend

En el proyecto Laravel:

php artisan serve --host=0.0.0.0 --port=8000



\### 3) Base URL (Emulador)

http://10.0.2.2:8000/api



> En dispositivo físico usar tu IP local (ej: http://192.168.X.X:8000/api)



---



\## 🔗 Deep Link

La app escucha:

gamingboost://payment-result



El backend redirige con ese deep link después del pago.



---



\## ✅ Funciones

\- Login

\- Catálogo de boosts

\- Crear pedidos

\- Checkout simulado (Custom Tabs)

\- Historial de pedidos con estados

\- Auto refresh al volver del deep link



---



\## 🧠 Backend API Repo

\- https://github.com/Danny8ac/GamingBoost-



---



\## 👨‍💻 Autor

Daniel Ochoa — Proyecto académico (2026)





