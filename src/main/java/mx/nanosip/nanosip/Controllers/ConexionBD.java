package mx.nanosip.nanosip.Controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
            "jdbc:sqlserver://nanozip-paulo-sql-2413.database.windows.net:1433;"
                    + "database=NanoZip;"
                    + "encrypt=true;"
                    + "trustServerCertificate=false;"
                    + "hostNameInCertificate=*.database.windows.net;"
                    + "loginTimeout=30;";

    private static final String USER = "NanoZipAppUser";
    private static final String PASSWORD = "NanoZipApp2026!";

    public static Connection getConexion() throws SQLException {
        int intentos = 3;
        SQLException ultimoError = null;

        for (int i = 1; i <= intentos; i++) {
            try {
                return DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                ultimoError = e;

                try {
                    System.out.println("Intento " + i + " fallido, reintentando...");
                    Thread.sleep(5000); // espera 5 segundos
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }

        throw ultimoError;
    }
}