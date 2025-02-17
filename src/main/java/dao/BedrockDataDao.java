package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import helpers.PooledDatasource;
import models.BedrockData;
import services.sentry.SentryService;

public class BedrockDataDao extends BaseDao {

    private Logger logger;

    public BedrockDataDao(PooledDatasource poolDs) {
        super(poolDs);
        this.tablename = "`wje_bedrock_data`";
        this.logger = Logger.getLogger("WJE:" + this.getClass().getSimpleName());
    }

    public JSONArray findWithUser(Integer userId) {

        JSONArray results = new JSONArray();

        try {
            String sql = "SELECT * FROM " + this.tablename + " WHERE user_id = ?";
            final PreparedStatement pstmt = this.getConnection().prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.executeQuery();

            final ResultSet resultSet = pstmt.getResultSet();
            results = resultSet == null ? null : this.toJsonArray(resultSet);
            pstmt.close();
            this.closeConnection();

        } catch (SQLException e) {
            SentryService.captureEx(e);
        }

        if (results == null || results.length() < 1) {
            return null;
        }


        return results;
    }

    @Override
    public Integer save(JSONObject sqlProps) {
        int id = sqlProps.optInt("id");
        int userId = sqlProps.optInt("user_id");
        final String pseudo = sqlProps.optString("pseudo");
        final String uuid = sqlProps.optString("uuid");
        final String msgId = sqlProps.optString("msg_id");
        final String createdAt = sqlProps.optString("created_at");
        final String updatedAt = sqlProps.optString("updated_at");

        final Object isConfirmed = sqlProps.opt("confirmed");
        final Object isAllowed = sqlProps.opt("allowed");

        final int allowed = isAllowed.equals(true) ? 1 : 0;
        final int confirmed = isConfirmed.equals(true) ? 1 : 0;

        String acceptedBy = sqlProps.optString("accepted_by");
        String revokedBy = sqlProps.optString("revoked_by");
        acceptedBy = acceptedBy.length() > 0 ? acceptedBy : null;
        revokedBy = revokedBy.length() > 0 ? revokedBy : null;

        if (userId < 1 || pseudo.equals("") || uuid.equals("") || msgId.equals("")) {
            this.logger.warning("Missing informations to save user entity");
            return -1;
        }

        if (acceptedBy == null && revokedBy == null) {
            this.logger.warning("Missing informations to save user entity");
            return -1;
        }

        try {
            int status = -1;
            BedrockData found = this.findWithUuid(uuid);

            // New user
            if (found == null) {
                id = -1;

                String sql = "INSERT INTO " + this.tablename + " (user_id, pseudo, uuid, accepted_by, " +
                        "revoked_by, msg_id, confirmed, allowed, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);";

                final PreparedStatement pstmt = this.getConnection().prepareStatement(sql, new String[] { "id" });
                pstmt.setInt(1, userId);
                pstmt.setString(2, pseudo);
                pstmt.setString(3, uuid);
                pstmt.setObject(4, acceptedBy);
                pstmt.setObject(5, revokedBy);
                pstmt.setString(6, msgId);
                pstmt.setInt(7, confirmed);
                pstmt.setInt(8, allowed);
                status = pstmt.executeUpdate();
                ResultSet generatedKeys = pstmt.getGeneratedKeys();

                while (generatedKeys.next()) {
                    id = generatedKeys.getInt(1);
                    break;
                }

                pstmt.close();
                this.closeConnection();
            }

            // Update User
            else {

                String sql = "UPDATE " + this.tablename + " SET " +
                        "user_id = ?," +
                        "pseudo = ?," +
                        "uuid = ?," +
                        "accepted_by = ?," +
                        "revoked_by = ?," +
                        "msg_id = ?," +
                        "confirmed = ?," +
                        "allowed = ? ," +
                        "updated_at = CURRENT_TIMESTAMP " +
                        "WHERE id = ?;";

                final PreparedStatement pstmt = this.getConnection().prepareStatement(sql);
                pstmt.setInt(1, userId);
                pstmt.setString(2, pseudo);
                pstmt.setString(3, uuid);
                pstmt.setObject(4, acceptedBy);
                pstmt.setObject(5, revokedBy);
                pstmt.setString(6, msgId);
                pstmt.setInt(7, confirmed);
                pstmt.setInt(8, allowed);
                pstmt.setObject(9, id);
                status = pstmt.executeUpdate();
                id = pstmt.getUpdateCount() > 0 ? id : -1;
                pstmt.close();
                this.closeConnection();

            }

        } catch (Exception e) {
            SentryService.captureEx(e);
        }

        return id;
    }

    public JSONArray findAllowed() {

        JSONArray results = new JSONArray();

        try {
            String sql = "SELECT * FROM " + this.tablename + " WHERE allowed = 1";
            final PreparedStatement pstmt = this.getConnection().prepareStatement(sql);
            pstmt.executeQuery();

            final ResultSet resultSet = pstmt.getResultSet();
            results = resultSet == null ? null : this.toJsonArray(resultSet);
            pstmt.close();
            this.closeConnection();

        } catch (SQLException e) {
            SentryService.captureEx(e);
        }

        if (results == null || results.length() < 1) {
            return null;
        }

        return results;
    }

    public void setPlayerUUID(Integer userId, UUID UUID, boolean tempConfirmed) {
        try {
            String sql = "UPDATE " + this.tablename + " SET mc_uuid = ? WHERE user_id = ?;";
            if (tempConfirmed) {
                sql = "UPDATE " + this.tablename + " SET mc_uuid = ?, confirmed = 1 WHERE user_id = ?;";
            }

            final PreparedStatement pstmt = this.getConnection().prepareStatement(sql);
            pstmt.setString(1, UUID.toString());
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            pstmt.close();
            this.closeConnection();

        } catch (SQLException e) {
            SentryService.captureEx(e);
        }
    }

    public BedrockData findWithUuid(String uuid) {

        JSONArray results = new JSONArray();

        try {
            String sql = "SELECT * FROM " + this.tablename + " WHERE uuid = ?";
            final PreparedStatement pstmt = this.getConnection().prepareStatement(sql);
            pstmt.setString(1, uuid);
            pstmt.executeQuery();

            final ResultSet resultSet = pstmt.getResultSet();
            results = resultSet == null ? null : this.toJsonArray(resultSet);
            pstmt.close();
            this.closeConnection();

        } catch (SQLException e) {
            SentryService.captureEx(e);
        }

        if (results == null || results.length() < 1) {
            return null;
        }

        return new BedrockData(results.getJSONObject(0));
    }

}
