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

    @PostMapping("/tienda/addProductoNew") 
    public ResponseEntity<String> addProductoNew(@RequestBody Producto producto) 
    {
	//Class.forName("org.postgresql.Driver");
	try{
	    
	    Connection conn = DriverManager.getConnection(url, user, pass);
	    PreparedStatement st = conn.prepareStatement("INSERT INTO productos (nombre, precio, cantidad) VALUES (?,?,?)");
	    st.setString(1, producto.getNombre());
		st.setFloat(2, producto.getPrecio());
		st.setInt(3, producto.getCantidad());

	    int rowsInsert = st.executeUpdate();
		
		if(rowsInsert > 0){
			ResultSet generarLlave = statement.getGenerateKeys();
			if(generarLlave.next()){
				int id = generarLlave.getInt(1);
				generarLlave.close();
				if( id == 0)
					return ResponseEntity.badRequest().body("Este producto ya existe en la DB");
				else 
					return ResponseEntity.ok("El producto se ha agregado correctamente. ID: "+ id);
			}
		}
		return ResponseEntity.badRequest().body("No se puedo generar el prodcuto correctamente");
	 
	    st.close();
	    conn.close();
	} catch (SQLException e) {
		System.out.println("Excepción SQL Exception: " + e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
		}
    }


    @PutMapping("/tienda/addProducto") 
    public ResponseEntity<String> addProducto(@RequestBody Producto producto) 
    {
	//Class.forName("org.postgresql.Driver");
	try{
	    
	    Connection conn = DriverManager.getConnection(url, user, pass);
	    PreparedStatement st = conn.prepareStatement("UPDATE productos SET nombre = ?, precio = ?, cantidad = ? WHERE id = ?");
	    st.setString(1, producto.getNombre());
		st.setFloat(2, producto.getPrecio());
		st.setInt(3, producto.getCantidad());
        st.setInt(4, producto.getId());

	    int rowsUpdate = st.executeUpdate();
		
		if(rowsInsert > 0){
			return ResponseEntity.ok("El producto se ha agregado correctamente. ID: "+ producto.getId());
		}
		return ResponseEntity.badRequest().body("No se puedo introducir el prodcuto correctamente");
	 
	    st.close();
	    conn.close();
	} catch (SQLException e) {
		System.out.println("Excepción SQL Exception: " + e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
		}
    }
    @DeleteMapping("/tienda/eliminarProducto") 
    public ResponseEntity<String> deleteProducto(@RequestBody Producto producto) 
    {
	//Class.forName("org.postgresql.Driver");
	try{
	    
	    Connection conn = DriverManager.getConnection(url, user, pass);
	    PreparedStatement st = conn.prepareStatement("DELETE FROM productos WHERE id = ?");
        st.setInt(1, producto.getId());

	    int rowsDeleted = st.executeUpdate();
		
		if(rowsDeleted > 0){
			return ResponseEntity.ok("El producto se ha elimando correctamente.");
		}
		return ResponseEntity.badRequest().body("No se puedo eliminar el prodcuto correctamente");
	 
	    st.close();
	    conn.close();
	} catch (SQLException e) {
		System.out.println("Excepción SQL Exception: " + e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
		}
    }
    @GetMapping("/tienda/CashFlow") 
    public ResponseEntity<String> cashFlow() 
    {
	//Class.forName("org.postgresql.Driver");
	try{
	    
	    Connection conn = DriverManager.getConnection(url, user, pass);
	    PreparedStatement st = conn.prepareStatement("SELECT SUM(cashFlow) AS flujo_caja FROM productos");

	    ResultSet rs = st.executeQuery();
		
		while(rs.next())
		{
		    float flujoDeCaja = rs.getFloat("flujo_caja");
			return ResponseEntity.ok("El flujo de caja de la tienda es: "+flujoDeCaja);
		}
	    rs.close();
		return ResponseEntity.badRequest().body("No se ha podido obtener el flujo de caja");
	 
	    st.close();
	    conn.close();
	} catch (SQLException e) {
		System.out.println("Excepción SQL Exception: " + e.getMessage());
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en el servidor");
		}
    }
}
