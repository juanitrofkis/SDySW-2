package com.example.producto;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.smartcardio.ResponseAPDU;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@RestController
public class ProductoController {
    private String url = "jdbc:postgresql://localhost:5432/dit";
    private String user = "dit";
    private String pass = "dit";

	private Connection conn;
	private Statement st; 
	
	private void initDatabase() {
		try{	
			conn = DriverManager.getConnection(url, user, pass);
			st = conn.createStatement();
		} catch (Exception e) {
				System.err.println(e.getClass().getName()+": "+e.getMessage());
				System.exit(0);
		}
}
	

/*****************************************************************************************************************
SACA UN PRODUCTO SEGUN EL ID y CANTIDAD QUE SE LE PASE 
*****************************************************************************************************************/
	@GetMapping("/tienda/comprar")
	public ResponseEntity<Producto> compraProducto(@RequestParam(value = "id") int id,
												@RequestParam(value = "cantidad") int cantidad) {
		initDatabase();
		Producto producto = null;
		try {
			ResultSet rs = st.executeQuery("SELECT * FROM INVENTARIO WHERE ID=" + id + ";");
			if (rs.next() && cantidad >= 0) {
				float precio = rs.getFloat("PRECIO");
				String nombre = rs.getString("NOMBRE");
				int cantidadDisponible = rs.getInt("CANTIDAD");

				if (cantidadDisponible >= cantidad) {
					int nuevaCantidad = cantidadDisponible - cantidad;
					producto = new Producto(precio, nombre, nuevaCantidad, id);
					st.executeUpdate("UPDATE INVENTARIO SET CANTIDAD=" + nuevaCantidad + " WHERE ID=" + id + ";");

					// Actualizar el cashFlow de la caja aquí
					//Obtengo el dinero total que hay en caja
					ResultSet rs3 = st.executeQuery("SELECT TOTAL FROM CUENTAS WHERE ID=0");
					if(rs3.next()){
						float cuenta = rs3.getFloat("TOTAL");

						//Nuevo valor de cashFlow = Lo que habia en caja + lo que cuesta el producto elegido por la cantidad comprada
						float total =cuenta+ (cantidad * precio);

						st.executeUpdate("UPDATE CUENTAS SET TOTAL="+total+" WHERE ID=0;");


						// Cerrar el ResultSet después de utilizar los datos
						rs3.close();
					}
					rs.close();

					return ResponseEntity.ok().body(producto);
				} else {
					System.out.println("No hay suficiente cantidad disponible del producto con el id "+id+". (se han intentado extraer "+cantidad+" elementos).");
				}
			} else {
				System.out.println("No se encontró un producto válido con el id "+id+".");
			}
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		} 

		return ResponseEntity.notFound().build();
	}






/*****************************************************************************************************************
DEVUELVE AL INVENTARIO UNA CANTIDAD DEL PRODUCTO CON LA ID PASADA 
*****************************************************************************************************************/
				
	@GetMapping("/tienda/devolver")
	public void devuelveProducto(@RequestParam (value = "id") int id, @RequestParam (value = "cantidad") int cantidad)
	{
		initDatabase();
		Producto producto = null;
		try {
			ResultSet rs = st.executeQuery("SELECT * FROM INVENTARIO WHERE ID=" + id + ";");
			if (rs.next()) {
				float precio = rs.getFloat("PRECIO");
				int cantidadDisponible = rs.getInt("CANTIDAD");

				int nuevaCantidad = cantidadDisponible + cantidad;
				st.executeUpdate("UPDATE INVENTARIO SET CANTIDAD=" + nuevaCantidad + " WHERE ID=" + id + ";");

				// Actualizar el cashFlow de la caja aquí
				//Obtengo el dinero total que hay en caja
				ResultSet rs3 = st.executeQuery("SELECT TOTAL FROM CUENTAS WHERE ID=0");
				if(rs3.next()){
					float cuenta = rs3.getFloat("TOTAL");

					//Nuevo valor de cashFlow = Lo que habia en caja - lo que cuesta el producto elegido por la cantidad comprada
					float total =cuenta - (cantidad * precio);

					st.executeUpdate("UPDATE CUENTAS SET TOTAL="+total+" WHERE ID=0;");


					// Cerrar el ResultSet después de utilizar los datos
					rs3.close();
				}
				rs.close();


			} else {
				System.out.println("No se encontró un producto válido con el id "+id+".");
			}
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		} 

	}
		

	
/*****************************************************************************************************************
DEVUELVE UNA LISTA CON TODOS LOS PRODUCTOS
*****************************************************************************************************************/
		
    @GetMapping("/tienda/lista")
    public List<Producto> listarProductos()
    {
		initDatabase();
        List<Producto> resultado = new LinkedList<Producto>();
		try{
			ResultSet rs = st.executeQuery("SELECT * FROM INVENTARIO ORDER BY ID;");

			while (rs.next()) {
				Producto producto = new Producto(rs.getFloat("PRECIO"), rs.getString("NOMBRE"), rs.getInt("CANTIDAD"), rs.getInt("ID"));
				resultado.add(producto);
			}
			
			rs.close();
		}catch(Exception e){
			System.err.println(e.getClass().getName()+": "+e.getMessage());

		}
        return resultado; //Devuelvo lista de Productos
    }

/*****************************************************************************************************************
AÑADE UN PRODUCTO NUEVO A LA BASE DE DATOS
*****************************************************************************************************************/

    @PostMapping("/tienda/addNewProduc") 
    public int addProductoNew(@RequestBody Producto producto) 
    {
		initDatabase();
		//Obtenemos el ID más alto y le sumamos 1
		int id=0;
		try{
			ResultSet rs = st.executeQuery("SELECT MAX(ID) AS ID FROM INVENTARIO;");
			
			
			
			if (rs.next() && producto.getCantidad() >= 1){
				id = rs.getInt("ID")+1;
				ResultSet rs2 = st.executeQuery("SELECT NOMBRE FROM INVENTARIO;");
				Boolean repetido = false;
				while(rs2.next())
					if (rs2.getString("NOMBRE").equals(producto.getNombre()))
						repetido = true;
				
				if (repetido) {
					System.out.println("Este producto ya existe en la base de datos");
				}
				else{
					st.executeUpdate("INSERT INTO INVENTARIO (ID,NOMBRE,PRECIO,CANTIDAD)"
									+ " VALUES("+id+",'"+producto.getNombre()+"',"+producto.getPrecio()+","+producto.getCantidad()+");");
					System.out.println("El producto se ha agregado correctamente. ID: "+ id);
				}
				rs2.close();
			} else if (producto.getCantidad() < 1)
				System.out.println("La cantidad tiene que ser mayor o igual a 1");
			
			
			rs.close();
		}catch(Exception e){
			System.err.println(e.getClass().getName()+": "+e.getMessage());

		}
		return id;
    }

/*****************************************************************************************************************
AÑADE UN PRODUCTO A LA BASE DE DATOS
*****************************************************************************************************************/

    @PostMapping("/tienda/addProd") 
    public void addProducto(@RequestBody Producto producto) 
    {
		initDatabase();
		int cantidad = producto.getCantidad();
		int id = producto.getId();
		try{
			ResultSet rs = st.executeQuery("SELECT CANTIDAD FROM INVENTARIO WHERE ID="+id+";");
			if (rs.next()) {
				st.executeUpdate("UPDATE INVENTARIO SET CANTIDAD="+(rs.getInt("CANTIDAD")+cantidad)+" WHERE ID="+id+";");
				
			}else
				System.out.println("No se ha podido añadir el producto con id '"+id+"'.");
			
			System.out.println("El producto se ha agregado correctamente. ID: "+id);
			rs.close();	
		}catch(Exception e){
			System.err.println(e.getClass().getName()+": "+e.getMessage());

		}
	
    }

/*****************************************************************************************************************
ELIMINA UN PRODUCTO A LA BASE DE DATOS
*****************************************************************************************************************/

    @GetMapping("/tienda/eliminarProd") 
    public void deleteProducto(@RequestParam int id) 
    {
		initDatabase();
		try{
			ResultSet rs = st.executeQuery("SELECT * FROM INVENTARIO WHERE ID ="+id);
			if (!rs.next())
				System.out.println("el producto con id '"+id+"' no existe.");
			else
				st.executeUpdate("DELETE FROM INVENTARIO WHERE ID="+id+";");
			rs.close();
		}catch(Exception e){
			System.err.println(e.getClass().getName()+": "+e.getMessage());

		}
		
    }

/*****************************************************************************************************************
OBTIENE EL FLUJO DE CAJA DE LA BASE DE DATOS
*****************************************************************************************************************/
    
	@GetMapping("/tienda/cashFlow") 
    public float cashFlow() 
    {
		initDatabase();
		float total=0;
		try{
			ResultSet rs = st.executeQuery("SELECT * FROM CUENTAS WHERE ID=0");
			
			while(rs.next())
			{
				total = rs.getFloat("TOTAL");
			}
			rs.close();
		}catch(Exception e){
			System.err.println(e.getClass().getName()+": "+e.getMessage());

		}
		return total;
    }

/*****************************************************************************************************************
CERRAMOS LA CONEXION A LA BASE DE DATOS
*****************************************************************************************************************/
	protected void finalize() throws Throwable{
		initDatabase();
		try {
			st.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName()+": "+e.getMessage());

		}
		try {
			conn.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName()+": "+e.getMessage());

		}
	}
	
}
