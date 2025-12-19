package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProfileDao;
import org.yearup.models.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao {
    public MySqlProfileDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile) {
        // this creates a profile when a user registers
        String sql = """
                INSERT INTO profiles
                (user_id, first_name, last_name, phone, email, address, city, state, zip)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);

            // map Profile object fields to SQL columns using ? to replace
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();
            return profile;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // added this so I can grab a user's profile using their userId
    // this is what GET /profile calls
    @Override
    public Profile getByUserId(int userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);

            // this will sub in the logged in user's id
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            // if we found a profile turn the DB row into a Profile object
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // If somehow no profile exists
        return null;
    }

    // added this so users can update their profile info
    @Override
    public void update(Profile profile) {
        String sql = """
                UPDATE profiles
                SET first_name = ?, last_name = ?, phone = ?, email = ?,
                    address = ?, city = ?, state = ?, zip = ?
                WHERE user_id = ?
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);

            // Update all editable profile fields
            ps.setString(1, profile.getFirstName());
            ps.setString(2, profile.getLastName());
            ps.setString(3, profile.getPhone());
            ps.setString(4, profile.getEmail());
            ps.setString(5, profile.getAddress());
            ps.setString(6, profile.getCity());
            ps.setString(7, profile.getState());
            ps.setString(8, profile.getZip());

            // user_id is used in the WHERE clause so users can only update their own profile
            ps.setInt(9, profile.getUserId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // created a helper method to avoid repeating mapping logic
    // basically converts a ResultSet row into a Profile object
    private Profile mapRow(ResultSet row) throws SQLException {
        return new Profile(row.getInt("user_id"), row.getString("first_name"), row.getString("last_name"), row.getString("phone"), row.getString("email"), row.getString("address"), row.getString("city"), row.getString("state"), row.getString("zip"));
    }
}