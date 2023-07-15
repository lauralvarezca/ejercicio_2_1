package com.example.ejercicio_2_1.Procesos;

public class Transacciones {
    public static final String NameDataBase = "Tarea";

    public static final String TablaVideo = "grabarvideo";

    public static final String id = "id";
    public static final String video = "video";

    public static final String CreateTableVideo = "CREATE TABLE grabarvideo (id INTEGER PRIMARY KEY AUTOINCREMENT,video BLOB)";
    public static final String DropTableVideo = "DROP TABLE IF EXISTS grabarvideo";
    public static final String test1 = "SELECT * FROM grabarvideo";
}
