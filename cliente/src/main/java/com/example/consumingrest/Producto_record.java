package com.example.consumingrest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Producto_record (int id, 
                        String nombre, 
                        int cantidad, 
                        float precio){ }
