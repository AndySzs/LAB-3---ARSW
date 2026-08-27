package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@Primary
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgresBlueprintPersistence(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        try {
            String checkSql = "SELECT COUNT(*) FROM blueprints WHERE author = ? AND name = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, bp.getAuthor(), bp.getName());
            if (count != null && count > 0) {
                throw new BlueprintPersistenceException("Blueprint already exists: " + bp.getAuthor() + ":" + bp.getName());
            }

            String insertBpSql = "INSERT INTO blueprints (author, name) VALUES (?, ?)";
            jdbcTemplate.update(insertBpSql, bp.getAuthor(), bp.getName());

            String insertPointSql = "INSERT INTO blueprint_points (author, blueprint_name, x, y) VALUES (?, ?, ?, ?)";
            for (Point p : bp.getPoints()) {
                // Se usa p.x() y p.y() porque Point es un record
                jdbcTemplate.update(insertPointSql, bp.getAuthor(), bp.getName(), p.x(), p.y());
            }
        } catch (Exception e) {
            if (e instanceof BlueprintPersistenceException) {
                throw (BlueprintPersistenceException) e;
            }
            throw new BlueprintPersistenceException("Error saving blueprint: " + e.getMessage());
        }
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        String bpSql = "SELECT author, name FROM blueprints WHERE author = ? AND name = ?";
        List<String> names = jdbcTemplate.query(bpSql, (rs, rowNum) -> rs.getString("name"), author, name);

        if (names.isEmpty()) {
            throw new BlueprintNotFoundException(String.format("Blueprint not found: %s/%s", author, name));
        }

        // Consultar los puntos del plano
        String pointSql = "SELECT x, y FROM blueprint_points WHERE author = ? AND blueprint_name = ?";
        List<Point> points = jdbcTemplate.query(pointSql, (rs, rowNum) -> new Point(rs.getInt("x"), rs.getInt("y")), author, name);

        // Crear el Blueprint pasándole los puntos por el constructor existente
        return new Blueprint(author, name, points);
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        String bpSql = "SELECT name FROM blueprints WHERE author = ?";
        List<String> blueprintNames = jdbcTemplate.query(bpSql, (rs, rowNum) -> rs.getString("name"), author);

        if (blueprintNames.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }

        Set<Blueprint> blueprints = new HashSet<>();
        for (String name : blueprintNames) {
            blueprints.add(getBlueprint(author, name));
        }

        return blueprints;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        String bpSql = "SELECT author, name FROM blueprints";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(bpSql);

        Set<Blueprint> blueprints = new HashSet<>();
        for (Map<String, Object> row : rows) {
            String author = (String) row.get("author");
            String name = (String) row.get("name");
            try {
                blueprints.add(getBlueprint(author, name));
            } catch (BlueprintNotFoundException e) {
                // Ignorar si no se encuentra en el loop
            }
        }

        return blueprints;
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        getBlueprint(author, name); // Valida que exista

        String insertPointSql = "INSERT INTO blueprint_points (author, blueprint_name, x, y) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(insertPointSql, author, name, x, y);
    }
}