package com.example.producto;
public class Producto {
    private final float precio;
    private final String nombre;
    private final int cantidad;
    private final int id;
    public Producto(float precio, String nombre, int cantidad, int id) {
        this.precio = precio;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.id = id;
    }
    public float getPrecio() {
	    return precio;
    }
    public String getNombre() {
	    return nombre;
    }
    public int getCantidad() {
	    return cantidad;
    }
    public int getId() {
	    return id;
    }
    public float setPrecio() {
	    return precio;
    }
    public String setNombre() {
	    return nombre;
    }
    public int setCantidad() {
	    return cantidad;
    }
    public int setId() {
	    return id;
    }
	
}
