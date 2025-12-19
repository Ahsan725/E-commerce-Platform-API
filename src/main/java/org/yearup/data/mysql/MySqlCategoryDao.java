package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    // pass the datasource up to the base dao so we can open connections
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories()
    {
        // this list will hold all categories pulled from the db
        List<Category> categories = new ArrayList<>();

        // simple select, no filters
        String sql = "SELECT * FROM categories;";

        // try with resources so connection auto-closes
        try (Connection connection = getConnection())
        {
            // prepare + execute query
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet row = statement.executeQuery();

            // loop through each row and convert it into a Category object
            while (row.next())
            {
                categories.add(mapRow(row));
            }
        }
        catch (SQLException e)
        {
            // bubble up db errors as runtime exceptions
            throw new RuntimeException(e);
        }

        return categories;
    }

    @Override
    public Category getById(int categoryId)
    {
        // fetch exactly one category by its primary key
        String sql = "SELECT * FROM categories WHERE category_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind the category id safely
            statement.setInt(1, categoryId);

            ResultSet row = statement.executeQuery();

            // if a row exists, map and return it
            if (row.next())
            {
                return mapRow(row);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        // return null if category doesn’t exist
        return null;
    }

    @Override
    public Category create(Category category)
    {
        // insert a new category
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?);";

        try (Connection connection = getConnection())
        {
            // request generated keys so we can get the new category id
            PreparedStatement statement =
                    connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            // bind values from the category object
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int rowsAffected = statement.executeUpdate();

            // if insert succeeded, grab the generated id
            if (rowsAffected > 0)
            {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next())
                {
                    int newCategoryId = generatedKeys.getInt(1);

                    // fetch the full category object from db and return it
                    return getById(newCategoryId);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        // return null if insert failed
        return null;
    }

    @Override
    public void update(int categoryId, Category category)
    {
        // update name + description for a specific category id
        String sql = "UPDATE categories " +
                "SET name = ?, description = ? " +
                "WHERE category_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind updated values
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            // specify which category to update
            statement.setInt(3, categoryId);

            // execute update no return
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int categoryId)
    {
        // remove category by id
        String sql = "DELETE FROM categories WHERE category_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            // bind category id
            statement.setInt(1, categoryId);

            // execute delete
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    // helper method to convert a db row into a category object
    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        // return a fully populated Category model
        return new Category(categoryId, name, description);
    }
}