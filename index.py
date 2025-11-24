from flask import Flask, render_template, request, redirect, url_for, session, send_file, flash
import psycopg2
from psycopg2.extras import RealDictCursor
from io import BytesIO
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import letter
import os
from dotenv import load_dotenv
import hashlib
from functools import wraps # Necesario para el decorador login_required

# Cargar variables del archivo .env
load_dotenv()

app = Flask(__name__)
# ¡IMPORTANTE! Reemplaza esto con una clave segura antes de la producción
app.secret_key = "TU_CLAVE_SECRETA_SUPER_SEGURA_AQUI" 

# ----------------------------------------
#   Configuración de la Base de Datos
# ----------------------------------------
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("DB_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASS = os.getenv("DB_PASS")

# ----------------------------------------
#   Función de conexión a PostgreSQL
# ----------------------------------------
def get_conn():
    """Establece y devuelve una conexión a la base de datos PostgreSQL."""
    try:
        return psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            database=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
    except Exception as e:
        print(f"Error al conectar a la base de datos: {e}")
        # En una aplicación real, se manejaría este error mejor
        raise e

# ======================================================
#             PROTECCIÓN PARA RUTAS (DECORADOR)
# ======================================================
def login_required(f):
    """Decorador para asegurar que una ruta solo sea accesible si el usuario ha iniciado sesión."""
    @wraps(f)
    def wrapper(*args, **kwargs):
        if "loggedin" not in session:
            flash("Debes iniciar sesión primero.", "warning")
            return redirect(url_for("login"))
        return f(*args, **kwargs)
    return wrapper


# ======================================================
#             RUTAS DE AUTENTICACIÓN
# ======================================================

# LOGIN (Ruta principal "/")
@app.route("/", methods=["GET", "POST"])
@app.route("/login", methods=["GET", "POST"]) # Añadimos /login para claridad, aunque / ya apunta aquí
def login():
    if request.method == "POST":
        correo = request.form["correo"]
        password = request.form["contraseña"]

        # Encriptar la contraseña para compararla con la BD
        hashed_password = hashlib.sha256(password.encode()).hexdigest()

        conn = None
        cur = None
        try:
            conn = get_conn()
            cur = conn.cursor(cursor_factory=RealDictCursor)
            cur.execute(
                "SELECT id_clientes, nombre FROM clientes WHERE correo = %s AND contraseña = %s",
                (correo, hashed_password)
            )
            cliente = cur.fetchone()
        finally:
            if cur: cur.close()
            if conn: conn.close()

        if cliente:
            session["loggedin"] = True
            session["id_cliente"] = cliente["id_clientes"]
            session["nombre_cliente"] = cliente["nombre"]
            flash(f"¡Bienvenido de nuevo, {cliente['nombre']}!", "success")
            return redirect(url_for("index"))

        flash("Correo o contraseña incorrectos.", "error")

    return render_template("login.html")


# RUTA DE REGISTRO
@app.route("/registro", methods=["GET", "POST"])
def registro():
    if request.method == "POST":
        
        nombre = request.form["nombre"]
        apellido = request.form["apellido"]
        correo = request.form["correo"]
        password = request.form["contraseña"]
        telefono = request.form["telefono"]
        
        # Encriptar la contraseña
        hashed_password = hashlib.sha256(password.encode()).hexdigest()

        conn = None
        cur = None
        try:
            conn = get_conn()
            cur = conn.cursor()
            
            cur.execute("""
                INSERT INTO clientes (nombre, apellido, correo, contraseña, telefono)
                VALUES (%s, %s, %s, %s, %s)
            """, (nombre, apellido, correo, hashed_password, telefono))
            
            conn.commit()
            flash("¡Registro exitoso! Por favor, inicia sesión.", "success")
            return redirect(url_for("login"))
            
        except psycopg2.IntegrityError:
            conn.rollback()
            flash("El correo ya está registrado o hay un error de integridad de datos.", "error")
            
        except Exception as e:
            conn.rollback()
            flash(f"Ocurrió un error al registrar: {e}", "error")
            
        finally:
            if cur: cur.close()
            if conn: conn.close()
    
    return render_template("registro.html")


# LOGOUT
@app.route("/logout")
def logout():
    session.clear()
    flash("Has cerrado sesión.", "info")
    return redirect(url_for("login"))


# ======================================================
#                 CATÁLOGO PRINCIPAL
# ======================================================

@app.route("/index")
@login_required
def index():
    conn = get_conn()
    cur = conn.cursor(cursor_factory=RealDictCursor)
    cur.execute("SELECT * FROM productos ORDER BY nombre")
    productos = cur.fetchall()
    cur.close()
    conn.close()

    return render_template("index.html", productos=productos)


# ======================================================
#                       CARRITO
# ======================================================

@app.route("/add_cart", methods=["POST"])
@login_required
def add_cart():
    """Agrega un producto al carrito de compras almacenado en la sesión."""
    try:
        product_id = request.form["id_producto"]
        cantidad = int(request.form["cantidad"])
    except (KeyError, ValueError):
        flash("Error al procesar la solicitud de agregar al carrito.", "error")
        return redirect(url_for("index"))

    if "cart" not in session:
        session["cart"] = {}

    carrito = session["cart"]
    # Sumar la cantidad si el producto ya existe en el carrito
    carrito[product_id] = carrito.get(product_id, 0) + cantidad
    session.modified = True # Asegura que Flask guarde el cambio
    
    flash(f"Producto {product_id} agregado al carrito.", "success")
    return redirect(url_for("index"))


@app.route("/carrito")
@login_required
def carrito():
    """Muestra el contenido del carrito, consultando detalles de productos de la BD."""
    if "cart" not in session or len(session["cart"]) == 0:
        return render_template("carrito.html", items=[], total=0)

    conn = get_conn()
    cur = conn.cursor(cursor_factory=RealDictCursor)

    items = []
    total = 0

    # Iterar sobre los IDs y cantidades guardados en la sesión
    for idp, qty in session["cart"].items():
        # idp en sesión es str, convertir a int para la consulta si es necesario, 
        # aunque psycopg2 lo maneja. Lo dejamos como str ya que así se almacena.
        cur.execute("SELECT * FROM productos WHERE id_producto = %s", (idp,))
        p = cur.fetchone()
        
        if p:
            subtotal = p["precio"] * qty
            items.append({"producto": p, "cantidad": qty, "subtotal": subtotal})
            total += subtotal

    cur.close()
    conn.close()

    return render_template("carrito.html", items=items, total=total)


# ======================================================
#               ELIMINAR PRODUCTO DEL CARRITO
# ======================================================
# Nota: La ruta duplicada ha sido eliminada. Esta es la versión corregida.
@app.route("/eliminar/<int:idp>")
@login_required
def eliminar(idp):
    """
    Elimina un producto del carrito (session['cart']) por su ID.
    idp es el ID del producto (id_producto) a eliminar.
    """
    idp_str = str(idp) 

    if "cart" in session:
        if idp_str in session["cart"]:
            # Eliminar la entrada del producto del diccionario del carrito
            session["cart"].pop(idp_str)
            
            # Marcar la sesión como modificada
            session.modified = True 
            
            flash(f"Producto eliminado del carrito.", "info")
        else:
            flash("Ese producto no se encontró en tu carrito.", "error")
    else:
        flash("Tu carrito está vacío.", "warning")

    # Redirigir de nuevo a la página del carrito para ver el estado actualizado
    # Si quieres volver al catálogo, cambia a url_for("index")
    return redirect(url_for("carrito")) 


# ======================================================
#             COMPRAR (USA ID DE CLIENTE EXISTENTE)
# ======================================================

@app.route("/comprar", methods=["GET", "POST"])
@login_required
def comprar():
    # 1. OBTENER ID DEL CLIENTE DE LA SESIÓN (YA ESTÁ LOGUEADO)
    id_cliente_existente = session.get("id_cliente") 
    
    if not id_cliente_existente:
        flash("Tu sesión expiró. Por favor, vuelve a iniciar sesión.", "error")
        return redirect(url_for("login"))
    
    if request.method == "GET":
        return render_template("comprar.html")

    codigo_postal = request.form.get("CP", "N/A") 
    
    conn = get_conn()
    cur = conn.cursor(cursor_factory=RealDictCursor)

    # Si es POST, procede a registrar la VENTA (no el cliente)

    conn = get_conn()
    cur = conn.cursor(cursor_factory=RealDictCursor)
    
    # 2. PROCESAR CADA ÍTEM DEL CARRITO EN LA TABLA VENTAS
    if "cart" in session and session["cart"]:
        
        try:
            for idp, qty in session["cart"].items():
                
                # Obtener precio y stock del producto
                cur.execute("SELECT precio, stock FROM productos WHERE id_producto = %s", (idp,))
                result = cur.fetchone()

                if not result:
                    flash(f"Error: Producto con ID {idp} no encontrado.", "error")
                    conn.rollback()
                    return redirect(url_for("carrito"))
                
                if result["stock"] < qty:
                    flash(f"Error: Stock insuficiente para el producto {idp}.", "error")
                    conn.rollback()
                    return redirect(url_for("carrito"))
                    
                precio = result["precio"]
                total_item = precio * qty

                # 3. INSERTAR LA VENTA
                cur.execute("""
                    INSERT INTO ventas (id_producto, id_clientes, cantidad, total, fecha_salida)
                    VALUES (%s, %s, %s, %s, CURRENT_DATE)
                """, (idp, id_cliente_existente, qty, total_item))

                # 4. ACTUALIZAR STOCK
                cur.execute("""
                    UPDATE productos SET stock = stock - %s WHERE id_producto = %s
                """, (qty, idp))

            conn.commit()
            
        except Exception as e:
            conn.rollback()
            flash(f"Ocurrió un error al procesar la compra: {e}", "error")
            return redirect(url_for("carrito"))
            
        finally:
            cur.close()
            conn.close()
        
    else:
        flash("Tu carrito de compras está vacío.", "warning")
        return redirect(url_for("index"))

    # 5. Limpiar el carrito y redirigir al ticket
    session.pop("cart", None)
    
    flash("¡Compra realizada con éxito! Generando su ticket.", "success")
    return redirect(url_for("ticket", id_cliente=id_cliente_existente))


# ======================================================
#                       TICKET PDF
# ======================================================

@app.route("/ticket/<int:id_cliente>")
@login_required
def ticket(id_cliente):
    conn = get_conn()
    cur = conn.cursor(cursor_factory=RealDictCursor)

    # Nota: La consulta sigue funcionando porque usamos JOIN clientes C 
    # para obtener el nombre del cliente existente.
    cur.execute("""
        SELECT c.nombre, c.apellido, v.id_producto, v.cantidad, v.total, v.fecha_salida,
        p.nombre AS nombre_producto
        FROM ventas v
        JOIN clientes c ON v.id_clientes = c.id_clientes
        JOIN productos p ON p.id_producto = v.id_producto
        WHERE v.id_clientes = %s
    """, (id_cliente,))

    ventas = cur.fetchall()
    cur.close()
    conn.close()

    if not ventas:
        flash("No se encontraron ventas para este cliente.", "error")
        return redirect(url_for("index"))

    buffer = BytesIO()
    pdf = canvas.Canvas(buffer, pagesize=letter)

    # 1. Encabezado del Ticket
    pdf.setFillColorRGB(0.36, 0.25, 0.62) # Morado (similar al diseño web)
    pdf.setFont("Helvetica-Bold", 20)
    pdf.drawString(50, 750, "THE 4 FANTASTIC- Ticket de Compra")
    
    pdf.setFillColorRGB(0.1, 0.1, 0.1) # Texto negro
    pdf.setFont("Helvetica", 12)
    y = 720
    
    # 2. Información del Cliente
    cliente = ventas[0]
    pdf.drawString(50, y, f"Cliente: {cliente['nombre']} {cliente['apellido']}")
    pdf.drawString(300, y, f"Fecha: {cliente['fecha_salida'].strftime('%d/%m/%Y')}")
    y -= 30
    
    # 3. Encabezados de la tabla
    pdf.setFont("Helvetica-Bold", 11)
    pdf.drawString(50, y, "PRODUCTO")
    pdf.drawString(300, y, "CANTIDAD")
    pdf.drawString(400, y, "TOTAL")
    y -= 15
    pdf.line(50, y, 550, y) # Línea divisoria
    y -= 15

    # 4. Detalle de Productos
    pdf.setFont("Helvetica", 11)
    subtotal_total = 0
    for v in ventas:
        # Asegúrate de que el total se formatee a dos decimales
        total_str = "{:,.2f}".format(v['total'])
        pdf.drawString(50, y, f"{v['nombre_producto']}")
        pdf.drawString(300, y, f"{v['cantidad']}")
        pdf.drawString(400, y, f"${total_str}")
        subtotal_total += v["total"]
        y -= 20
        
        if y < 50: # Manejo de múltiples páginas
            pdf.showPage()
            pdf.setFont("Helvetica", 12)
            y = 750

    # 5. Total Final
    pdf.line(50, y - 10, 550, y - 10) # Línea divisoria
    pdf.setFont("Helvetica-Bold", 14)
    pdf.setFillColorRGB(0.36, 0.25, 0.62) # Morado para el total
    pdf.drawString(300, y - 30, "TOTAL PAGADO:")
    pdf.drawString(400, y - 30, f"${'{:,.2f}'.format(subtotal_total)}")

    pdf.save()
    buffer.seek(0)

    # Devolver el PDF
    return send_file(buffer, as_attachment=True, download_name="ticket.pdf", mimetype="application/pdf")


# ======================================================
#                       EJECUCIÓN
# ======================================================

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5025, debug=True)