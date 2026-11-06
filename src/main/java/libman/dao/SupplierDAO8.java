package libman.dao;

import libman.model.Supplier8;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierDAO8 extends DAO8 {
    public List<Supplier8> searchByName(String keyword) throws SQLException {
        String sql = "SELECT supplierId, supplierName, address, phone, email FROM tblSupplier8 "
                + "WHERE supplierName LIKE ? ESCAPE '=' ORDER BY supplierName";
        List<Supplier8> suppliers = new ArrayList<>();
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + escapeLike(keyword.trim()) + "%");
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    suppliers.add(map(result));
                }
            }
        }
        return suppliers;
    }

    public Optional<Supplier8> findById(long supplierId) throws SQLException {
        String sql = "SELECT supplierId, supplierName, address, phone, email FROM tblSupplier8 WHERE supplierId = ?";
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, supplierId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public Supplier8 insert(Supplier8 supplier) throws SQLException {
        String sql = "INSERT INTO tblSupplier8 (supplierName, address, phone, email) VALUES (?, ?, ?, ?)";
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, supplier.getSupplierName());
            statement.setString(2, supplier.getAddress());
            statement.setString(3, supplier.getPhone());
            statement.setString(4, supplier.getEmail());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    supplier.setSupplierId(keys.getLong(1));
                    return supplier;
                }
            }
        }
        throw new SQLException("Không lấy được mã nhà cung cấp sau khi lưu.");
    }

    private Supplier8 map(ResultSet result) throws SQLException {
        Supplier8 supplier = new Supplier8();
        supplier.setSupplierId(result.getLong("supplierId"));
        supplier.setSupplierName(result.getString("supplierName"));
        supplier.setAddress(result.getString("address"));
        supplier.setPhone(result.getString("phone"));
        supplier.setEmail(result.getString("email"));
        return supplier;
    }

    private String escapeLike(String value) {
        return value.replace("=", "==").replace("%", "=%").replace("_", "=_");
    }
}
