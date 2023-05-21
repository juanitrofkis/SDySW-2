package com.example.consumingrest;
import com.example.producto.Producto;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;


import java.util.Scanner;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;
import java.sql.*;
import java.util.Date;


@SpringBootApplication
public class ConsumingRestApplication {

	/**
	 * Método que inicializa la ejecución del software Spring
	 * @param args
	 */
    public static void main(String[] args) {
		SpringApplication.run(ConsumingRestApplication.class, args);
    }


	

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
	return builder.build();
    }

	/**
	 * Método requestProductoRest, que hace una petición REST a localhost:8080
	 * con los argumentos que se le pasen y devuelve un objeto producto_record.
	 * @param args
	 * @return producto_record
	 */
	private static Producto_record requestProductoRest(String args){
		Producto_record producto_record = null;
		RestTemplate restTemplate = new RestTemplate();
		try{
			producto_record = restTemplate.getForObject(
				String.format("http://localhost:8080/%s", args), Producto_record.class);
		}catch(Exception e){ }
		return producto_record;
	}

	/**
	 * Método run que realiza las interacciones con el usuario final.
	 * @param restTemplate
	 * @return
	 * @throws Exception
	 */
    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
	return args -> {
		int opcion = 0;
		Scanner scanner = new Scanner(System.in);

		System.out.println("Seleccione una de las siguientes opciones:\n");
		while(true){
			System.out.println("\n1. Comprar\n2. Lista\n3. Devolver\n4. Opciones de Administración\n5. Salir\n");
			try{
			opcion = scanner.nextInt();
			
			switch (opcion) {
				case 1: // ✔
					System.out.println("Has seleccionado la opcion de COMPRAR\n");
					System.out.println("Ingrese el Id del producto que quiera comprar: ");
					int id = scanner.nextInt();
					System.out.println("Ingrese la cantidad del producto a comprar: ");
					int cantidad = scanner.nextInt();
					if(cantidad < 0){
							System.out.println("No se puede introducir un número negativo\n");      
					}
					else{   
						Producto_record p = requestProductoRest(String.format("tienda/comprar?id=%d&cantidad=0", id));
						if (p!=null){
							float precio_compra = p.precio();
							precio_compra = cantidad*precio_compra;
							if(cantidad <= p.cantidad()){
								requestProductoRest(String.format("tienda/comprar?id=%d&cantidad=%d", id, cantidad));
								System.out.println("Se ha efectuado la compra del producto: "+ p.nombre() + " con un coste total: "+ precio_compra);
							}else{
								System.out.println("No se ha podido efectuar la compra. Se han solicitado más productos de los que hay disponibles ("+cantidad+">"+p.cantidad()+").");
							}
						}else{
							System.out.println("No existe ningún producto con el id "+id);
						}
					} 
					break;

				case 2: // ✔
					System.out.println("Has seleccionado la opcion de LISTAR PRODUCTOS\n");

					ParameterizedTypeReference<List<Producto>> responseType = new ParameterizedTypeReference<List<Producto>>() {};
					List<Producto> listaProducto = restTemplate.exchange("http://localhost:8080/tienda/lista", HttpMethod.GET, null, responseType).getBody();

					//List<Producto> listaProducto = restTemplate.getForObject("http://localhost:8080/tienda/lista", List.class);
					String resultado = "◢__ID__.________NOMBRE________.__PRECIO__._CANTIDAD_◣";

					
					for(Producto i: listaProducto) {
						String fila = String.format("| %4d | %-20s | %8.2f | %-8d |", 
									i.getId(),i.getNombre(), 
									i.getPrecio(),i.getCantidad());
						resultado = resultado +"\n"+fila;
					}

					System.out.println(resultado+"\n");
					break;

				case 3: // ✔
					System.out.println("Has seleccionado la opcion de DEVOLVER\n");
					System.out.println("Ingrese el Id del producto que desea devolver: ");
					int id_producto_devolver = scanner.nextInt();
					System.out.println("Ingrese la cantidad del producto a devolver: ");
					int cantidad_producto_devolver = scanner.nextInt();
					if(cantidad_producto_devolver < 0){
						System.out.println("No se puede introducir un número negativo\n");	
					}
					else{
						Producto_record p = requestProductoRest(String.format("/tienda/comprar?id=%d&cantidad=0", id_producto_devolver));
						if(p!=null){
							float precio_devolver = p.precio();
							precio_devolver = cantidad_producto_devolver*precio_devolver;
							if(precio_devolver < 0){
								System.out.println("No se puede introducir un número negativo\n");
							}
							else{
								requestProductoRest(String.format("/tienda/devolver?id=%d&cantidad=%d", id_producto_devolver, cantidad_producto_devolver));
								System.out.println("Se ha efectuado la devolución del producto: "+ id_producto_devolver);
								
							}
						}else{
							System.out.println("No existe ningún producto con el id "+id_producto_devolver);
						}
					}
					break;
				case 4:
					System.out.println("Has seleccionado la opcion de OPCIONES DE ADMINISTRACION\n");
					int opcion1=0;
					boolean salir_2 = false;
					while(!salir_2){

						System.out.println("\n1. Añadir nuevo producto");
						System.out.println("2. Añadir producto");
						System.out.println("3. Eliminar producto");
						System.out.println("4. Ver flujo de caja");
						System.out.println("5. Salir\n");
						try{
							System.out.println("Elija una de las anteriores opciones:\n");
							opcion1 = scanner.nextInt();
							switch (opcion1) {
								case 1: // ✔? - no estoy seguro si debería ser Producto o ProductoController
									System.out.println("Has seleccionado la opcion de INTRODUCIR NUEVO PRODUCTO\n");
									scanner.nextLine();
									System.out.println("Ingrese el nombre del producto a introducir: ");
									String nombre = scanner.nextLine();
									System.out.println("Ingrese la cantidad del producto a introducir: ");
									int cantidad_intr = scanner.nextInt();

									if (cantidad_intr < 0){
										System.out.println("No se puede introducir un número negativo\n");
									}
									else{
										System.out.println("Ingrese el precio del producto a introducir: ");
										float precio = scanner.nextFloat();
										Producto_record producto = new Producto_record(0, nombre, cantidad_intr, precio);
										restTemplate.postForObject("http://localhost:8080/tienda/addNewProduc", producto, int.class);
										
									}
									break;

								case 2: // ✔
									System.out.println("Has seleccionado la opcion de AÑADIR PRODUCTO\n");
									System.out.println("Ingrese el Id del producto que quiera añadir: ");
									int id_producto_añadir = scanner.nextInt();
									System.out.println("Ingrese la cantidad del producto a añadir: ");
									int cantidad_producto_añadir = scanner.nextInt();

									if (cantidad_producto_añadir < 0){
										System.out.println("No se puede introducir un número negativo\n");
									}
									else{
										Producto_record p = requestProductoRest(String.format("/tienda/comprar?id=%d&cantidad=0", id_producto_añadir));
										if (p!=null){
											Producto_record producto = new Producto_record(id_producto_añadir, p.nombre(), cantidad_producto_añadir, p.precio());
											restTemplate.postForObject("http://localhost:8080/tienda/addProd", producto, Producto_record.class);
										}else{
											System.out.println("No existe ningún producto con el id "+id_producto_añadir);
										}
									}
									break;

								case 3: // ✔ - Un poco matar moscas a cañonazos el tener que pasar el producto entero para eliminarlo...
									System.out.println("Has seleccionado la opcion de ELIMINAR\n");
									System.out.println("Ingrese el Id del producto que quiera eliminar: ");
									int id_producto_eliminar = scanner.nextInt();
									restTemplate.getForObject(String.format("http://localhost:8080/tienda/eliminarProd?id=%d", id_producto_eliminar), ResponseEntity.class);
									break;
								
								
								case 4: // ✔?
									System.out.println("Has seleccionado la opcion de VER FLUJO DE CAJA\n");
									float dinero = restTemplate.getForObject("http://localhost:8080/tienda/cashFlow", float.class);
									System.out.println(String.format("Dinero en caja: %.2f€.", dinero));
									break;
								case 5: // ✔
									salir_2 = true;
									break;
								default:
									System.out.println("Solo números entre 1 y 5");
								
							}
						} catch (Exception e) {
							System.err.println("Excepcion en ConsumingRestApplication:");
							e.printStackTrace();
						}
					}		
				break;
				case 5: // ✔
					System.exit(0);
					break;
				default: // ✔
					System.out.println("Solo números entre 1 y 5");
					break;
			}
			
			} catch (Exception e ){
				System.err.println("Excepción en ConsumingRestApplication: ");
				e.printStackTrace();
			}
		}
	};
    }
}



