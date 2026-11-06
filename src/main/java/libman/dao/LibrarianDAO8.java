package libman.dao;

import libman.model.Librarian8;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class LibrarianDAO8 extends DAO8 {
    public Optional<Librarian8> findById(long librarianId) throws SQLException {
        String sql = "SELECT librarianId, fullName, username, phone, email FROM tblLibrarian8 WHERE librarianId = ?";
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, librarianId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                Librarian8 librarian = new Librarian8();
                librarian.setLibrarianId(result.getLong("librarianId"));
                librarian.setFullName(result.getString("fullName"));
                librarian.setUsername(result.getString("username"));
                librarian.setPhone(result.getString("phone"));
                librarian.setEmail(result.getString("email"));
                return Optional.of(librarian);
            }
        }
    }
}
