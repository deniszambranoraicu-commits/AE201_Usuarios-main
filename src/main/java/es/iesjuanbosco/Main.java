package es.iesjuanbosco;

import java.sql.*;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        // Variable para sacar el id
        System.out.println("Introduce el id del usuario el cual quieras saber su telefono");
        int cod = sc.nextInt();

        // La base de datos está en el directorio de trabajo del proyecto.
        String url = "jdbc:sqlite:prueba.db";
        String sql = "select * from usuarios where localidad=? ";
        String sql2 = "SELECT u.cod ,t.telefono FROM usuarios u JOIN Telefonos t ON u.cod = t.cod\n" +
                "WHERE u.cod = " + cod;

        try (Connection conexion = DriverManager.getConnection(url);        // Conectar a la base de datos
             PreparedStatement stmt = conexion.prepareStatement(sql);       // Crear un PreparedStatement
             ResultSet rs = stmt.executeQuery();                             // Ejecutar consulta
             PreparedStatement stmt2 = conexion.prepareStatement(sql2);      // Crear un PreparedStatement
             ResultSet rs2 = stmt2.executeQuery()) {                        // Ejecutar consulta

            System.out.println("Conexión establecida con éxito.");

            // Mostrar resultados de la primera consulta
            System.out.println("\nDatos de usuarios:");
            while (rs.next()) {
                int codigo = rs.getInt("cod");
                String nombre = rs.getString("nombre");
                String apellidos = rs.getString("apellidos");
                String direccion = rs.getString("direccion");
                String localidad = rs.getString("localidad");

                System.out.println(codigo + " | " + nombre + " | " + apellidos + " | " + direccion + " | " + localidad);
            }

            // Mostrar resultado del teléfono consultado por ID (sql2)
            System.out.println("\nTeléfono del usuario:");
            while (rs2.next()) {
                int codigoUser = rs2.getInt("cod");
                String telefono = rs2.getString("telefono");

                System.out.println("ID: " + codigoUser + " | Teléfono: " + telefono);
            }

        } catch (SQLException e) {
            System.out.println("Error al conectar o consultar la base de datos:");
            e.printStackTrace();
        }
    }
}