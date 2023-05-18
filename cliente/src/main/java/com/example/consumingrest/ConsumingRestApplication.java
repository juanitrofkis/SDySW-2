package com.example.consumingrest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;
import java.sql.*;
import java.util.Date;


@SpringBootApplication
public class ConsumingRestApplication {

    private static final Logger log = LoggerFactory.getLogger(ConsumingRestApplication.class);

	/**
	 * Método que inicializa la ejecución del software Spring
	 * @param args
	 */
    public static void main(String[] args) {
		SpringApplication.run(ConsumingRestApplication.class, args);
    }


	/**
	 * Método requestProductoRest, que hace una petición REST a localhost:8080
	 * con los argumentos que se le pasen y devuelve un objeto producto_record.
	 * @param args
	 * @return producto_record
	 */
	private static Producto_record requestProductoRest(String args){
		Producto_record producto_record = restTemplate.getForObject(
			String.format("http://localhost:8080/%s", args), Producto_record.class);
		log.info(producto_record.toString());
		return producto_record;
	}

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
	return builder.build();
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
		boolean salir = false;

		System.out.println("Seleccione una de las siguientes opciones:\n");
		while(!salir){
			System.out.println("\n1. Comprar\n2. Lista\n 3. Devolver\n4. Opciones de Administración\n 5. Salir\n");
			try{
			opcion = scanner.nextInt();
			
			switch (opcion) {
				case 1: // ✔
					System.out.println("Has seleccionado la opcion de COMPRAR\n");
					System.out.println("Ingrese el Id del producto que quiera comprar: ");
					int id_producto_compra = EntradaDatos.nextInt();
					System.out.println("Ingrese la cantidad del producto a comprar: ");
					int cantidad_producto_comprar = EntradaDatos.nextInt();
					if(cantidad_producto_comprar < 0){
							System.out.println("No se puede introducir un número negativo\n");      
					}
					else{   
						Producto_record p = requestProductoRest(String.format("/tienda/compra?id=%d&cantidad=0", id));
						float precio_compra = p.precio;
						precio_compra = cantidad_producto_comprar*precio_compra;
						requestProductoRest(String.format("/tienda/compra?id=%d&cantidad=%d", id, cantidad));
						System.out.println("Se ha efectuado la compra del producto: "+ p.nombre + " con un coste total: "+ precio_compra);
					} 
					break;

				case 2: // ✔? - Esta lista no me da muy buena espina... - ¿A lo mejor con POST?
					System.out.println("Has seleccionado la opcion de LISTAR PRODUCTOS\n");
					List<Producto_record> listaProducto = restTemplate.getForObject("http://localhost:8080/tienda/lista", List.class);
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
					int id_producto_devolver = EntradaDatos.nextInt();
					System.out.println("Ingrese la cantidad del producto a devolver: ");
					int cantidad_producto_devolver = EntradaDatos.nextInt();
					if(cantidad_producto_devolver < 0){
						System.out.println("No se puede introducir un número negativo\n");	
					}
					else{
						Producto_record p = requestProductoRest(String.format("/tienda/compra?id=%d&cantidad=0", id));
						float precio_devolver = p.precio;
						precio_devolver = cantidad_producto_devolver*precio_devolver;
						if(precio_devolver < 0){
							System.out.println("No se puede introducir un número negativo\n");
						}
						else{
							Producto_record p = requestProductoRest(String.format("/tienda/devolver?id=%d&cantidad=%d", id_producto_devolver, cantidad_producto_devolver));
							System.out.println("Se ha efectuado la devolución del producto: "+ id_producto_devolver);
							c.log(fecha+" "+args[3] +" "+"se ha devuelto " + cantidad_producto_devolver+ " del producto con id: "+ id_producto_devolver);
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
							opcion1 = EntradaDatos.nextInt();
							switch (opcion1) {
								case 1: // ✔? - no estoy seguro si debería ser Producto o ProductoController
									System.out.println("Has seleccionado la opcion de INTRODUCIR NUEVO PRODUCTO\n");
									EntradaDatos.nextLine();
									System.out.println("Ingrese el nombre del producto a introducir: ");
									String nombre_producto_introducir = EntradaDatos.nextLine();
									System.out.println("Ingrese la cantidad del producto a introducir: ");
									int cantidad_producto_introducir = EntradaDatos.nextInt();

									if (cantidad_producto_introducir < 0){
										System.out.println("No se puede introducir un número negativo\n");
									}
									else{
										System.out.println("Ingrese el precio del producto a introducir: ");
										float precio_producto_introducir = EntradaDatos.nextFloat();
										Producto producto = new Producto(precio_producto_introducir, nombre_producto_introducir, cantidad_producto_introducir, 0);
										ResponseEntity<String> respuesta = restTemplate.postForObject("http://localhost:8080/tienda/addNewProduc", producto, Producto.Class);
										System.out.println("Respuesta del servidor: "+ respuesta.toString());
										
									}
									break;

								case 2: // ✔
									System.out.println("Has seleccionado la opcion de AÑADIR PRODUCTO\n");
									System.out.println("Ingrese el Id del producto que quiera añadir: ");
									int id_producto_añadir = EntradaDatos.nextInt();
									System.out.println("Ingrese la cantidad del producto a añadir: ");
									int cantidad_producto_añadir = EntradaDatos.nextInt();

									if (cantidad_producto_añadir < 0){
										System.out.println("No se puede introducir un número negativo\n");
									}
									else{
										Producto_record p = requestProductoRest(String.format("/tienda/compra?id=%d&cantidad=0", id_producto_añadir));
										Producto producto = new Producto(p.precio, p.nombre, cantidad_producto_añadir, id_producto_añadir);
										ResponseEntity<String> respuesta = restTemplate.postForObject("http://localhost:8080/tienda/addProd", producto, Producto.Class);
										System.out.println("Respuesta del servidor: "+ respuesta.toString());
									}
									break;

								case 3: // ✔ - Un poco matar moscas a cañonazos el tener que pasar el producto entero para eliminarlo...
									System.out.println("Has seleccionado la opcion de ELIMINAR\n");
									System.out.println("Ingrese el Id del producto que quiera eliminar: ");
									int id_producto_eliminar = EntradaDatos.nextInt();
									ResponseEntity<String> respuesta = restTemplate.getForObject(String.format("http://localhost:8080/tienda/cashFlow?id=%d", id_producto_eliminar), ResponseEntity.class);
									System.out.println("Respuesta del servidor: "+ respuesta.toString());
									break;
								
								
								case 4: // ✔?
									System.out.println("Has seleccionado la opcion de VER FLUJO DE CAJA\n");
									ResponseEntity<String> respuesta = restTemplate.getForObject("http://localhost:8080/tienda/cashFlow", ResponseEntity.class);
									System.out.println("Respuesta del servidor: "+ respuesta.toString());
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
					salir = true;
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


/*
Producto_record producto_record = restTemplate.getForObject(
								"http://localhost:8080/greeting", Producto_record.class);
			log.info(producto_record.toString());
 */