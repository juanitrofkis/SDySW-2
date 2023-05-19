
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

	Connection conn;
	Statement st; 
	
	public ProductoController() {
    //Producto producto = new Producto();
	
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
    public Producto compraProducto(@RequestParam (value = "id") int id, @RequestParam (value = "cantidad") int cantidad) 
    {
		Producto producto = null;
		ResultSet rs = st.executeQuery("SELECT * FROM INVENTARIO WHERE ID="+id+ ";");
		ResultSet rs2 = st.executeQuery("SELECT PRECIO FROM INVENTARIO WHERE ID=" +id+ ";");
		if (rs.next() && cantidad >= 0) {
			if (cantidad >=1){
				producto = new Producto(rs.getFloat("PRECIO"), rs.getString("NOMBRE"), cantidad, rs.getInt("ID"));
				int n = rs.getInt("CANTIDAD");
				if (n>=cantidad) {
					// se decrementa el número de unidades
					st.executeUpdate("UPDATE INVENTARIO SET CANTIDAD="+(n-cantidad)+" WHERE ID="+id+";");
				} else {
					System.out.println ("No se ha podido actualizar la cantidad");
				} 
			}
		}

		//Actualizamos el cashFlow de la caja, en este caso, al comprar, la caja aumenta su efectivo.

		//Obtengo precio del producto con la id determinada
		float precioProducto = rs2.getFloat("PRECIO");

		//Obtengo el dinero total que hay en caja
		ResultSet rs3 = st.executeQuery("SELECT TOTAL FROM CUENTAS WHERE ID=0");
		float cuenta = rs3.getFloat("TOTAL");

		if((0-precioProducto) > cuenta) 
		{
			System.out.println("Se ha intentado extraer una cantidad de cambio superior al dinero almacenado");	
		}
		//Nuevo valor de cashFlow = Lo que habia en caja + lo que cuesta el producto elegido por la cantidad comprada
		float total =cuenta+ (cantidad * precioProducto);

		st.executeUpdate("UPDATE CUENTAS SET TOTAL="+total+" WHERE ID=0;");

		rs.close();
		rs2.close();
		rs3.close();

		return producto;
	}
		


/*****************************************************************************************************************
DEVUELVE AL INVENTARIO UNA CANTIDAD DEL PRODUCTO CON LA ID PASADA 
*****************************************************************************************************************/
				
	@PutMapping("/tienda/devolver")
	public void devuelveProducto(@RequestParam (value = "id") int id, @RequestParam (value = "cantidad") int cantidad)
	{
			
		ResultSet rs = st.executeQuery("SELECT CANTIDAD FROM INVENTARIO WHERE ID="+id+";");
		ResultSet rs2 = st.executeQuery("SELECT PRECIO FROM INVENTARIO WHERE ID=" +id+ ";");
		if (rs.next()) {
			st.executeUpdate("UPDATE INVENTARIO SET CANTIDAD="+(rs.getInt("CANTIDAD")+cantidad)+" WHERE ID="+id+";");
		}
		else
			System.out.println("No se ha podido añadir a la base de datos la cantidad: "+cantidad);
					
		//Actualizamos el cashFlow de la caja, en este cajo, al devolver un producto, disminuye el efectivo en caja. 
		float precioProducto = rs2.getFloat("PRECIO");
		ResultSet rs3 = st.executeQuery("SELECT TOTAL FROM CUENTAS WHERE ID=0");
		float cuenta = rs3.getFloat("TOTAL");
		if((0-precioProducto) > cuenta)
		{ 
			System.out.println("Se ha intentado extraer una cantidad de cambio superior al dinero almacenado");	
		}
		
		//Compruebo que la cantidad de dinero a devolver no sea superior al efectivo en caja
		if ((cuenta - (cantidad * precioProducto)) < 0 )
		{
			System.out.println("No hay suficiente efectivo en caja para efectuar la devolucion")
		}
		else{

			float total = cuenta - (cantidad * precioProducto);
			st.executeUpdate("UPDATE CUENTAS SET TOTAL="+total+" WHERE ID=0;");
			System.out.println("Se ha efectuado de manera correcta la devolución con ID: "+ id);
		}
		

		rs.close();
		rs2.close();
		rs3.close();

		
	}
		

	
/*****************************************************************************************************************
DEVUELVE UNA LISTA CON TODOS LOS PRODUCTOS
*****************************************************************************************************************/
		
    @GetMapping("/tienda/lista")
    public List<Producto> listarProductos()
    {
        List<Producto> resultado = new LinkedList<Producto>();
		ResultSet rs = st.executeQuery("SELECT * FROM INVENTARIO;");

        while (rs.next()) {
			Producto producto = new Producto(rs.getFloat("PRECIO"), rs.getString("NOMBRE"), rs.getInt("CANTIDAD"), rs.getInt("ID"));
			resultado.add(producto);
		}
		
		rs.close();
        return resultado; //Devuelvo lista de Productos
    }

/*****************************************************************************************************************
AÑADE UN PRODUCTO NUEVO A LA BASE DE DATOS
*****************************************************************************************************************/

    @PostMapping("/tienda/addNewProduc") 
    public int addProductoNew(@RequestBody Producto producto) 
    {
		//Obtenemos el ID más alto y le sumamos 1
		int id=0;
		
		ResultSet rs = st.executeQuery("SELECT MAX(ID) AS ID FROM INVENTARIO;");
		
		
		if (rs.next() && cantidad >= 1){
			id = rs.getInt("ID")+1;
			ResultSet rs2 = st.executeQuery("SELECT NOMBRE FROM INVENTARIO;");
			Boolean repetido = false;
			while(rs2.next())
				if (rs2.getString("NOMBRE").equals(producto.getNombre()))
					repetido = true;
			
			if (repetido) {
				System.out.println("Este producto ya existe en la base de datos");
			}
			else
				st.executeUpdate("INSERT INTO INVENTARIO (ID,NOMBRE,PRECIO,CANTIDAD)"
								+ " VALUES("+id+",'"+?+"',"+?+","+?+");");
				st.setString(1, producto.getNombre());
				st.setFloat(2, producto.getPrecio());
				st.setInt(3, producto.getCantidad());
		} else if (producto.getCantidad() < 1)
			System.out.println("La cantidad tiene que ser mayor o igual a 1");
		System.out.println("El producto se ha agregado correctamente. ID: "+ id);
		rs2.close();
		rs.close();
		return id;
    }

/*****************************************************************************************************************
AÑADE UN PRODUCTO A LA BASE DE DATOS
*****************************************************************************************************************/

    @PutMapping("/tienda/addProd") 
    public void addProducto(@RequestBody Producto producto) 
    {
		
		int cantidad = producto.getCantidad();
		int id = producto.getId();
		ResultSet rs = st.executeQuery("SELECT CANTIDAD FROM INVENTARIO WHERE ID="+id+";");
		if (rs.next()) {
			st.executeUpdate("UPDATE INVENTARIO SET CANTIDAD="+(rs.getInt("CANTIDAD")+cantidad)+" WHERE ID="+id+";");
			
		}else
			System.out.println("No se ha podido añadir el producto con id '"+id+"'.");
		
		System.out.println("El producto se ha agregado correctamente. ID: "+id);
		rs.close();	
	
    }

/*****************************************************************************************************************
ELIMINA UN PRODUCTO A LA BASE DE DATOS
*****************************************************************************************************************/

    @GetMapping("/tienda/eliminarProd") 
    public void deleteProducto(@RequestParam int id) 
    {
	    ResultSet rs = st.executeQuery("SELECT * FROM INVENTARIO WHERE ID ="+id);
		if (!rs.next())
			System.out.println("el producto con id '"+id+"' no existe.");
		else
			st.executeUpdate("DELETE FROM INVENTARIO WHERE ID="+id+";");
		rs.close();
		
    }

/*****************************************************************************************************************
OBTIENE EL FLUJO DE CAJA DE LA BASE DE DATOS
*****************************************************************************************************************/
    
	@GetMapping("/tienda/cashFlow") 
    public float cashFlow() 
    {

			ResultSet rs = st.executeQuery("SELECT TOTAL FROM CUENTAS WHERE ID=0");
			
			while(rs.next())
			{
				float total = rs.getFloat("flujo_caja");
				return total;
			}
			rs.close();
    }

/*****************************************************************************************************************
CERRAMOS LA CONEXION A LA BASE DE DATOS
*****************************************************************************************************************/
	protected void finalize() throws Throwable{
		try {
			st.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
		try {
			conn.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
		}
	}
	
}
