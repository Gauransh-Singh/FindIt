package com.findit.dao;

import com.findit.util.DBConnection;
import java.sql.*;
import java.util.*;

public class ItemDAO {
  public List<Map<String,Object>> categories() throws SQLException {
    List<Map<String,Object>> out=new ArrayList<>();
    try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("SELECT category_id,category_name FROM categories ORDER BY category_name"); ResultSet r=p.executeQuery()) {
      while(r.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("id",r.getInt(1));m.put("name",r.getString(2));out.add(m);} }
    return out;
  }
  public int create(int userId,int categoryId,String type,String name,String description,String color,String location,java.sql.Date date) throws SQLException {
    String q="INSERT INTO items(user_id,category_id,report_type,item_name,description,color,location,item_date,status) VALUES(?,?,?,?,?,?,?,?, 'OPEN')";
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q,Statement.RETURN_GENERATED_KEYS)){
      p.setInt(1,userId);p.setInt(2,categoryId);p.setString(3,type);p.setString(4,name);p.setString(5,description);p.setString(6,color);p.setString(7,location);p.setDate(8,date);p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){return r.next()?r.getInt(1):-1;}}
  }
  public List<Map<String,Object>> list(String type,String search,int userId,boolean mine) throws SQLException {
    List<Map<String,Object>> out=new ArrayList<>(); StringBuilder q=new StringBuilder("SELECT i.item_id,i.user_id,i.report_type,i.item_name,i.description,i.color,i.location,i.item_date,i.status,i.created_at,c.category_name,u.full_name FROM items i JOIN categories c ON c.category_id=i.category_id JOIN users u ON u.user_id=i.user_id WHERE 1=1");
    if(type!=null&&!type.isBlank())q.append(" AND i.report_type=?"); if(search!=null&&!search.isBlank())q.append(" AND (i.item_name LIKE ? OR i.description LIKE ? OR i.location LIKE ? OR c.category_name LIKE ?)");if(mine)q.append(" AND i.user_id=?");q.append(" ORDER BY i.created_at DESC");
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q.toString())){int n=1;if(type!=null&&!type.isBlank())p.setString(n++,type);if(search!=null&&!search.isBlank()){String s="%"+search.trim()+"%";for(int i=0;i<4;i++)p.setString(n++,s);}if(mine)p.setInt(n,userId);try(ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object> m=new LinkedHashMap<>();String[] cols={"item_id","user_id","report_type","item_name","description","color","location","item_date","status","created_at","category_name","full_name"};for(String col:cols)m.put(col,r.getObject(col));out.add(m);}}}return out;
  }
  public Map<String,Object> get(int id) throws SQLException {List<Map<String,Object>> a=list(null,"",0,false);for(Map<String,Object> m:a)if(((Number)m.get("item_id")).intValue()==id)return m;return null;}
  public List<Map<String,Object>> matches(int id) throws SQLException {
    Map<String,Object> item=get(id);if(item==null)return Collections.emptyList();
    String opposite="LOST".equals(item.get("report_type"))?"FOUND":"LOST";
    String q="SELECT i.item_id,i.report_type,i.item_name,i.description,i.color,i.location,i.item_date,i.status,c.category_name FROM items i JOIN categories c ON c.category_id=i.category_id WHERE i.report_type=? AND i.category_id=(SELECT category_id FROM items WHERE item_id=?) AND i.item_id<>? AND i.status<>'RESOLVED' ORDER BY ABS(DATEDIFF(i.item_date,(SELECT item_date FROM items WHERE item_id=?))),i.created_at DESC LIMIT 8";
    List<Map<String,Object>> out=new ArrayList<>();try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setString(1,opposite);p.setInt(2,id);p.setInt(3,id);p.setInt(4,id);try(ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object> m=new LinkedHashMap<>();for(String k:new String[]{"item_id","report_type","item_name","description","color","location","item_date","status","category_name"})m.put(k,r.getObject(k));out.add(m);}}}return out;
  }
  public boolean claim(int itemId,int userId,String answer) throws SQLException {
    try(Connection c=DBConnection.getConnection();PreparedStatement check=c.prepareStatement("SELECT user_id,status FROM items WHERE item_id=?")){check.setInt(1,itemId);try(ResultSet r=check.executeQuery()){if(!r.next()||r.getInt("user_id")==userId||"RESOLVED".equals(r.getString("status")))return false;}}
    try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO claims(item_id,claimant_id,verification_answer) VALUES(?,?,?)")){p.setInt(1,itemId);p.setInt(2,userId);p.setString(3,answer);return p.executeUpdate()==1;}
  }
  public List<Map<String,Object>> claims() throws SQLException {List<Map<String,Object>> out=new ArrayList<>();String q="SELECT cl.claim_id,cl.item_id,cl.claimant_id,cl.verification_answer,cl.status,cl.created_at,i.item_name,i.report_type,u.full_name claimant_name FROM claims cl JOIN items i ON i.item_id=cl.item_id JOIN users u ON u.user_id=cl.claimant_id ORDER BY cl.created_at DESC";try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q);ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object> m=new LinkedHashMap<>();for(String k:new String[]{"claim_id","item_id","claimant_id","verification_answer","status","created_at","item_name","report_type","claimant_name"})m.put(k,r.getObject(k));out.add(m);}}return out;}
  public boolean updateClaim(int claimId, String status) throws SQLException {
    if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) return false;

    try (Connection c = DBConnection.getConnection()) {
      c.setAutoCommit(false);
      try {
        try (PreparedStatement p = c.prepareStatement(
            "UPDATE claims SET status=? WHERE claim_id=? AND status='PENDING'")) {
          p.setString(1, status);
          p.setInt(2, claimId);
          if (p.executeUpdate() != 1) {
            c.rollback();
            return false;
          }
        }

        if ("APPROVED".equals(status)) {
          try (PreparedStatement p = c.prepareStatement(
              "UPDATE items SET status='RESOLVED' WHERE item_id=(SELECT item_id FROM claims WHERE claim_id=?)")) {
            p.setInt(1, claimId);
            p.executeUpdate();
          }
          try (PreparedStatement p = c.prepareStatement(
              "UPDATE claims SET status='REJECTED' WHERE item_id=(SELECT item_id FROM (SELECT item_id FROM claims WHERE claim_id=?) x) AND claim_id<>? AND status='PENDING'")) {
            p.setInt(1, claimId);
            p.setInt(2, claimId);
            p.executeUpdate();
          }
        }
        c.commit();
        return true;
      } catch (SQLException ex) {
        c.rollback();
        throw ex;
      } finally {
        c.setAutoCommit(true);
      }
    }
  }
  public boolean resolve(int itemId,int userId,boolean admin) throws SQLException {String q="UPDATE items SET status='RESOLVED' WHERE item_id=? AND status<>'RESOLVED'"+(admin?"":" AND user_id=?");try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(q)){p.setInt(1,itemId);if(!admin)p.setInt(2,userId);return p.executeUpdate()==1;}}
  public int count(String table) throws SQLException {if(!Arrays.asList("users","items","claims").contains(table))throw new IllegalArgumentException();try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM "+table);ResultSet r=p.executeQuery()){r.next();return r.getInt(1);}}
}
