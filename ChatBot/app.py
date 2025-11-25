from flask import Flask, request, jsonify
from flask_cors import CORS
import psycopg2
import os

app = Flask(__name__)
CORS(app) 

DB_NAME = "BD_tenis"
DB_USER = "postgres"
DB_PASS = "admin"
DB_HOST = "localhost"
DB_PORT = "5432"

INTENCIONES_CLAVE = {
    "precio": "precio", "costo": "precio", "valor": "precio", "cuanto": "precio",
    "stock": "stock", "disponibilidad": "stock", "existencias": "stock",
    "talla": "talla", "numero": "talla", "medida": "talla",
    "color": "color", "colores": "color",
    "descripcion": "descripcion", "caracteristicas": "descripcion", "detalles": "descripcion",
    "marca": "marca", "busco": "nombre"
}

# Conexión y Búsqueda 

def get_db_connection():
    """Intenta establecer y devolver una conexión a PostgreSQL."""
    try:
        conn = psycopg2.connect(
            dbname=DB_NAME, user=DB_USER, password=DB_PASS, host=DB_HOST, port=DB_PORT
        )
        return conn
    except psycopg2.OperationalError as e:
        print("------------------------------------------------------------------")
        print(f"❌ ERROR CRÍTICO DE CONEXIÓN A POSTGRESQL: {e}")
        print("Verifica las credenciales en 'app.py' y que el servicio de PostgreSQL esté activo.")
        print("------------------------------------------------------------------")
        return None

def buscar_producto(nombre_buscado):
    """Busca un producto por nombre o marca y devuelve sus características."""
    conn = get_db_connection()
    if conn is None:
        return {"error": "Error de conexión con la fuente de datos. Verifica el servidor."}

    try:
        cur = conn.cursor()
        query = """
            SELECT nombre, marca, talla, color, precio, stock, descripcion
            FROM productos 
            WHERE lower(nombre) LIKE %s OR lower(marca) LIKE %s
            LIMIT 1;
        """
        search_term = f"%{nombre_buscado.lower().strip()}%"
        cur.execute(query, (search_term, search_term))
        resultado = cur.fetchone()
        cur.close()
        conn.close()

        if resultado:
            return {
                "nombre": resultado[0], "marca": resultado[1], "talla": resultado[2], 
                "color": resultado[3], "precio": resultado[4], "stock": resultado[5], 
                "descripcion": resultado[6],
            }
        else:
            return {"mensaje": f"Advertencia: No se encontró el producto o marca relacionado con '{nombre_buscado}'. Intenta ser más específico."}

    except Exception as e:
        print(f"ERROR: Fallo al ejecutar la consulta SQL: {e}")
        return {"error": "Error interno del sistema al consultar productos."}

# --- Funciones de Utilidad General 

def consultar_precio_promedio():
    """Consulta y devuelve el precio promedio."""
    conn = get_db_connection()
    if conn is None: return {"respuesta": "Error: No se pudo conectar a la base de datos para calcular el promedio."}
    try:
        cur = conn.cursor()
        query = "SELECT ROUND(AVG(precio), 2) AS promedio_precio, COUNT(nombre) AS total_productos FROM productos;"
        cur.execute(query)
        resultado = cur.fetchone()
        cur.close()
        conn.close()
        
        if resultado and resultado[1] > 0:
            promedio = resultado[0]
            total = resultado[1]
            return {"respuesta": f"El **precio promedio** de los {total} productos en nuestro inventario es de **${promedio}**."}
        else:
            return {"respuesta": "Actualmente, no tenemos productos con precio registrado en el inventario para calcular un promedio."}
    except Exception as e:
        print(f"ERROR: Fallo al calcular precio promedio: {e}")
        return {"respuesta": "Error interno al procesar la solicitud de promedio de precios."}

def listar_inventario():
    """Consulta y devuelve un listado de todos los productos y su stock."""
    conn = get_db_connection()
    if conn is None: return {"respuesta": "Error: No se pudo conectar a la base de datos para listar el inventario."}
    try:
        cur = conn.cursor()
        query = "SELECT nombre, marca, stock FROM productos ORDER BY stock DESC, nombre ASC LIMIT 5;" # Limitado a 5 para no saturar
        cur.execute(query)
        resultados = cur.fetchall()
        cur.close()
        conn.close()

        if resultados:
            respuesta_lista = "Actualmente, estos son algunos de los productos clave en inventario:<br><ul>"
            for nombre, marca, stock in resultados:
                estado = "Disponible" if stock > 0 else "Agotado"
                respuesta_lista += f"<li>**{nombre}** ({marca}): {stock} unidades. Estado: {estado}.</li>"
            respuesta_lista += "</ul><br>Puedes preguntar por el precio o detalles de uno de ellos."
        else:
            respuesta_lista = "Nuestro inventario está vacío. Por favor, vuelve a consultar más tarde."
            
        return {"respuesta": respuesta_lista}
    except Exception as e:
        print(f"ERROR: Fallo al listar el inventario: {e}")
        return {"respuesta": "Error interno al procesar la solicitud de inventario."}
        
def obtener_atributos_generales(atributo):
    """Obtiene todos los valores únicos de un atributo (talla, color) para ofrecer una respuesta amplia."""
    conn = get_db_connection()
    if conn is None: return None
    try:
        cur = conn.cursor()
        query = f"SELECT DISTINCT {atributo} FROM productos WHERE {atributo} IS NOT NULL ORDER BY {atributo} ASC;"
        cur.execute(query)
        resultados = cur.fetchall()
        cur.close()
        conn.close()
        
        if resultados:
            # Convierte los resultados a una lista plana y los une
            valores = [str(r[0]) for r in resultados]
            return ", ".join(valores)
        return None
    except Exception:
        return None

# --- Endpoint Principal ---

@app.route('/api/chatbot', methods=['POST'])
def chatbot_endpoint():
    data = request.json
    consulta_usuario = data.get('mensaje', '').lower()

    if not consulta_usuario:
        return jsonify({"respuesta": "El mensaje no puede estar vacío."})

    # 1. Identificar Intención y extraer palabras clave
    intencion_identificada = None
    palabras_importantes = consulta_usuario.split()
    
    for palabra_clave, columna in INTENCIONES_CLAVE.items():
        if palabra_clave in palabras_importantes:
            intencion_identificada = columna
            break

    palabras_a_ignorar = set(INTENCIONES_CLAVE.keys()) | {'dame', 'quiero', 'saber', 'del', 'los', 'las', 'que', 'es', 'un', 'una', 'por', 'favor', 'acerca', 'de', 'el', 'la', 'cuál', 'modelo', 'tenis', 'producto', 'disponibles', 'hay'}

    nombre_buscado = " ".join([
        palabra for palabra in palabras_importantes 
        if palabra not in palabras_a_ignorar
    ]).strip()


    # 2. Manejo de Preguntas Generales
    if "promedio" in consulta_usuario and "precio" in consulta_usuario:
        return jsonify(consultar_precio_promedio())
        
    if "productos" in consulta_usuario or "stock" in consulta_usuario or "inventario" in consulta_usuario:
        return jsonify(listar_inventario())

    if "tallas" in consulta_usuario or ("talla" in consulta_usuario and not nombre_buscado):
        tallas = obtener_atributos_generales('talla')
        if tallas:
            return jsonify({"respuesta": f"Actualmente, tenemos tallas disponibles en el siguiente rango: **{tallas}**. ¿Qué producto te interesa?"})
        else:
            return jsonify({"respuesta": "No encontramos tallas registradas en este momento."})
            
    if "colores" in consulta_usuario or ("color" in consulta_usuario and not nombre_buscado):
        colores = obtener_atributos_generales('color')
        if colores:
            return jsonify({"respuesta": f"Los colores principales que manejamos son: **{colores}**. ¿Qué modelo buscas?"})
        else:
            return jsonify({"respuesta": "No encontramos colores registrados en este momento."})


    # 3. Búsqueda de Producto Específico
    if not nombre_buscado:
        respuesta = "Lo siento, necesito que me indiques el **nombre del producto** o la **marca** que deseas consultar para poder ayudarte."
        return jsonify({"respuesta": respuesta})

    info_producto = buscar_producto(nombre_buscado)

    if "error" in info_producto:
        return jsonify({"respuesta": info_producto["error"]})
    if "mensaje" in info_producto:
        # 4. Respuesta de Falla Inteligente (Si no encuentra el producto)
        respuesta_falla = info_producto["mensaje"]
        
        # Ofrecer opciones si la búsqueda falló, pero tenemos inventario
        tallas_disp = obtener_atributos_generales('talla')
        if tallas_disp:
             respuesta_falla += f"<br>Sin embargo, tenemos tallas disponibles: **{tallas_disp}**."
        
        return jsonify({"respuesta": respuesta_falla})


    # 5. Generar Respuesta Basada en Intención o Completa
    producto_nombre = info_producto['nombre']
    
    if intencion_identificada and intencion_identificada != 'nombre':
        # Respuesta ESPECÍFICA (ej. Solo precio)
        valor = info_producto.get(intencion_identificada, "No disponible")
        
        if intencion_identificada == 'stock':
            valor_formato = f"{valor} unidades. Estado: {'Disponible' if valor > 0 else 'Agotado'}."
        elif intencion_identificada == 'precio':
            valor_formato = f"${valor}"
        else:
            valor_formato = valor
            
        respuesta = (
            f"La información que solicitas para **{producto_nombre}** ({info_producto['marca']}) es: **{valor_formato}**."
        )
    else:
        # Respuesta COMPLETA
        stock_estado = "Disponible" if info_producto['stock'] > 0 else "Agotado"
        
        respuesta = (
            f"Aquí tienes los detalles completos del producto **{producto_nombre}** ({info_producto['marca']}):<br>"
            f"&nbsp;* **Precio:** ${info_producto['precio']} <br>"
            f"&nbsp;* **Talla (Real):** {info_producto['talla']} <br>"
            f"&nbsp;* **Color:** {info_producto['color']} <br>"
            f"&nbsp;* **Stock:** {stock_estado} ({info_producto['stock']} unidades) <br>"
            f"&nbsp;* **Descripción:** {info_producto['descripcion']}"
        )

    return jsonify({"respuesta": respuesta})

if __name__ == '__main__':
    print("Iniciando servidor Flask. Acceso en http://127.0.0.1:5000")
    app.run(host='127.0.0.1', port=5000)