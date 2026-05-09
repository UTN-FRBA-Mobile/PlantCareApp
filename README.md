# 🌱 PlantCareApp 

Aplicación móvil Android para el monitoreo inteligente de plantas mediante sensores de humedad de suelo.

Proyecto desarrollado para la materia **Desarrollo de Aplicaciones Moviiles** — Universidad Tecnológica Nacional, Facultad Regional Buenos Aires (UTN-FRBA).

---

## Descripción

PlantCareApp permite a los usuarios registrar y monitorear sus plantas en tiempo real. La app se conecta a sensores físicos de humedad de suelo y a un backend REST para obtener métricas actualizadas, mostrando el estado de salud de cada planta y alertando cuando necesita riego.

---

## Funcionalidades

- **Vista general de plantas** — listado de todas las plantas registradas con estado de salud, nivel de humedad y ubicación.
- **Agregar planta** — formulario para registrar una nueva planta con nombre, especie, ubicación y sensor asociado.
- **Integración con sensores** — lectura de datos de humedad del suelo en tiempo real a través del backend (simulados).
- **Estado de salud** — clasificación automática: Saludable, Estrés moderado, Estrés alto.

---

## Cómo correr el proyecto

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/UTN-FRBA-Mobile/PlantCareApp.git
   ```
2. Abrir el proyecto en **Android Studio Hedgehog** o superior.
3. Sincronizar Gradle (`File → Sync Project with Gradle Files`).
4. Ejecutar en un emulador o dispositivo físico con Android 6.0+.

---

## Equipo

Proyecto grupal — UTN-FRBA, 1er cuatrimestre 2026, Curso: K5061 
Matias Morales
Francisco Molina
Bruno Massaccese
Matias Moll

---

## Licencia

Uso académico. Ver [LICENSE](LICENSE).
