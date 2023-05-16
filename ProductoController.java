package com;


import java.util.concurrent.atomic.AtomicLong;
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

@RestController
public class ProductoController {
    private String url = "jdbc:postgresql://localhost:5432/dit";
    private String user = "dit";
    private String pass = "dit";
	try{	
		Connection conn = DriverManager.getConnection(url, user, pass);
		Statement st = conn.createStatement();
	} catch (Exception e) {
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
	}
		

    @PostMapping("/tienda/addProductoNew") 
    public ResponseEntity<String> addProductoNew(@RequestBody Producto producto) 
    {
		//Obtenemos el ID más alto y le sumamos 1
		int id=0;
		
		ResultSet rs = st.executeQuery("SELECT MAX(ID) AS ID FROM INVENTARIO;");
		
		
		if (rs.next() && cantidad >= 1) {
			id = rs.getInt("ID")+1;
			ResultSet rs2 = st.executeQuery("SELECT NOMBRE FROM INVENTARIO;");
			Boolean repetido = false;
			while(rs2.next())
				if (rs2.getString("NOMBRE").equals(producto.getNombre()))
					repetido = true;
			
			if (repetido) {
				return ResponseEntity.badRequest().body("Este producto ya existe en la base de datos");
			}
			else
				st.executeUpdate("INSERT INTO INVENTARIO (ID,NOMBRE,PRECIO,CANTIDAD)"
								+ " VALUES("+id+",'"+?+"',"+?+","+?+" );");
				st.setString(1, producto.getNombre());
				st.setFloat(2, producto.getPrecio());
				st.setInt(3, producto.getCantidad());
		} else if (producto.getCantidad() < 1)
			return ResponseEntity.badRequest().body("La cantidad tiene que ser mayor o igual a 1");
		return ResponseEntity.ok("El producto se ha agregado correctamente. ID: "+ id);
		rs2.close();
		rs.close();
    }


    @PutMapping("/tienda/addProducto") 
    public ResponseEntity<String> addProducto(@RequestBody Producto producto) 
    {
		
		int cantidad = producto.getCantidad();
		int id = producto.getId();
		ResultSet rs = st.executeQuery("SELECT CANTIDAD FROM INVENTARIO WHERE ID="+id+";");
		st.setInt(1, producto.getId());
		if (rs.next()) {
			st.executeUpdate("UPDATE INVENTARIO SET CANTIDAD="+(rs.getInt("CANTIDAD")+cantidad)+" WHERE ID="+id+";");
			
		}else
			return ResponseEntity.badRequest().body("No se puedo introducir el prodcuto correctamente");
		
		return ResponseEntity.ok("El producto se ha agregado correctamente. ID: "+ producto.getId());
		rs.close();	
	
    }
    @DeleteMapping("/tienda/eliminarProducto") 
    public ResponseEntity<String> deleteProducto(@RequestBody Producto producto) 
    {
	    int id = producto.getId();
	    ResultSet rs = st.executeQuery("SELECT * FROM INVENTARIO WHERE ID ="+id);
		if (!rs.next())
			return ResponseEntity.ok("El producto se ha elimando correctamente.");
		else
			st.executeUpdate("DELETE FROM INVENTARIO WHERE ID="+id+";");
		rs.close();
		
    }
    @GetMapping("/tienda/CashFlow") 
    public ResponseEntity<String> cashFlow() 
    {

			ResultSet rs = st.executeQuery("SELECT TOTAL FROM CUENTAS WHERE ID=0");
			
			while(rs.next())
			{
				float total = rs.getFloat("flujo_caja");
				return ResponseEntity.ok("El flujo de caja de la tienda es: "+total);
			}
			rs.close();
			return ResponseEntity.badRequest().body("No se ha podido obtener el flujo de caja");
		 
		
    }
	//cerramos la conexión a la base de datos
	protected void finalize() throws Throwable{
		try {
			st.close();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
		}
		try {
			conn.close();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
		}
	}
	
}
