package com.example.mqttappsensor;
// Clase usuario para almacenar los datos ;)
public class Usuario {
    public String nombre;
    public String edad;
    public String sexo;
    public String nacionalidad;
    public String estadoCivil;

    public Usuario(String nombre, String edad, String sexo, String nacionalidad, String estadoCivil) {
        this.nombre = nombre;
        this.edad = edad;
        this.sexo = sexo;
        this.nacionalidad = nacionalidad;
        this.estadoCivil = estadoCivil;
    }
}

