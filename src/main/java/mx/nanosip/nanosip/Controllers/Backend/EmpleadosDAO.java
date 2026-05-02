package mx.nanosip.nanosip.Controllers.Backend;

import mx.nanosip.nanosip.Controllers.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpleadosDAO {

    // ─── CREATE ───────────────────────────────────────────
    public void guardar(Empleados emp) {

        String sql = "INSERT INTO Empleados (Nombre, Puesto, RFC, CURP, Edad, Contrasena, Permisos) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getPuesto());
            ps.setString(3,  emp.getRfc().toUpperCase());
            ps.setString(4,  emp.getCurp().toUpperCase());
            ps.setByte  (5,  emp.getEdad());
            ps.setString(6,  emp.getContrasena());
            ps.setString  (7,  emp.getPermisos());
            ps.executeUpdate();

        } catch ( SQLException e) {
            e.printStackTrace();
        }
    }

    // ─── UPDATE ───────────────────────────────────────────
    public void actualizar(Empleados emp) {

        String sql = "UPDATE Empleados SET Nombre=?, Puesto=?, RFC=?, "
                + "CURP=?, Edad=?, Permisos=?, Contrasena=? WHERE ID=?";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getPuesto());
            ps.setString(3,  emp.getRfc().toUpperCase());
            ps.setString(4,  emp.getCurp().toUpperCase());
            ps.setByte  (5,  emp.getEdad());
            ps.setString  (6,  emp.getPermisos());
            ps.setString(7,emp.getContrasena());
            ps.setInt   (8, emp.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─── DELETE ───────────────────────────────────────────
    public void eliminar(Empleados emp) {
        String sql = "DELETE FROM Empleados WHERE ID = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, emp.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet obtenerTodos() throws SQLException {
        String sql = "SELECT * FROM Empleados ORDER BY Nombre";
        Connection con = ConexionBD.getConexion();
        return con.prepareStatement(sql).executeQuery();
    }
}
